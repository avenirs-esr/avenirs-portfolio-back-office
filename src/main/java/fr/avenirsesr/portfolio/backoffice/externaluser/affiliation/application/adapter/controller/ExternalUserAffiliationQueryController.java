package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationOverviewDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserDetailDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserGlobalAuthorizationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper.ExternalUserAffiliationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliatedExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserDetail;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.CofolioProfileClient;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only endpoints authenticated by login and guarded by permissions. Provisioning is exposed to
 * the institutions by {@link ExternalUserAffiliationProvisioningController}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/external-users")
public class ExternalUserAffiliationQueryController {

  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final CofolioProfileClient cofolioProfileClient;

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/affiliations")
  public ResponseEntity<PagedResponse<ExternalUserAffiliationOverviewDTO>> getAffiliations(
      @RequestParam(required = false) String keyword,
      @Parameter(array = @ArraySchema(schema = @Schema(ref = "#/components/schemas/EUserCategory")))
          @RequestParam(required = false)
          List<EUserCategory> categories,
      @RequestParam(required = false) List<UUID> institutionIds,
      @RequestParam(required = false) List<UUID> groupIds,
      @RequestParam(required = false) Boolean cofolioStaff,
      @RequestParam(required = false) Boolean cofolioStudent,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Getting the affiliated users (keyword: {}, categories: {}, institutions: {}, groups: {},"
            + " cofolioStaff: {}, cofolioStudent: {}) {}",
        keyword,
        categories,
        institutionIds,
        groupIds,
        cofolioStaff,
        cofolioStudent,
        pageCriteria);

    PagedResult<AffiliatedExternalUser> affiliatedExternalUsers =
        externalUserAffiliationService.findAll(
            keyword,
            categories,
            institutionIds,
            groupIds,
            Boolean.TRUE.equals(cofolioStaff),
            Boolean.TRUE.equals(cofolioStudent),
            pageCriteria);
    List<AffiliatedExternalUser> content = affiliatedExternalUsers.content();
    Map<String, CofolioProfile> cofolioProfiles =
        cofolioProfileClient.findByEppns(
            content.stream().map(user -> user.externalUser().getEppn()).distinct().toList());
    Map<UUID, Integer> usersCountsByGroupId =
        externalUserAffiliationService.countExternalUsersByGroupIdsWithSubGroups(
            content.stream()
                .flatMap(user -> user.groups().stream())
                .map(Group::getId)
                .distinct()
                .toList());

    return ResponseEntity.ok(
        new PagedResponse<>(
            content.stream()
                .map(
                    affiliatedExternalUser ->
                        ExternalUserAffiliationApplicationMapper
                            .toExternalUserAffiliationOverviewDTO(
                                affiliatedExternalUser,
                                cofolioProfiles.get(
                                    affiliatedExternalUser.externalUser().getEppn()),
                                usersCountsByGroupId))
                .toList(),
            PageInfoDTO.fromDomain(affiliatedExternalUsers.pageInfo())));
  }

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/{externalUserId}/detail")
  public ResponseEntity<ExternalUserDetailDTO> getExternalUser(@PathVariable UUID externalUserId) {
    log.debug("Getting the detail of external user: {}", externalUserId);

    ExternalUserDetail detail =
        externalUserAffiliationService.getExternalUserDetail(externalUserId);
    CofolioProfile cofolioProfile =
        cofolioProfileClient
            .findByEppns(List.of(detail.externalUser().getEppn()))
            .get(detail.externalUser().getEppn());

    return ResponseEntity.ok(
        ExternalUserAffiliationApplicationMapper.toExternalUserDetailDTO(detail, cofolioProfile));
  }

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/{externalUserId}/global-authorization")
  public ResponseEntity<ExternalUserGlobalAuthorizationDTO> getGlobalAuthorizationOfExternalUser(
      @PathVariable UUID externalUserId) {
    log.debug("Getting the global authorization of external user: {}", externalUserId);

    return ResponseEntity.ok(
        ExternalUserAffiliationApplicationMapper.toExternalUserGlobalAuthorizationDTO(
            externalUserAffiliationService.getExternalUserGlobalAuthorization(externalUserId)));
  }

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/{externalUserId}/affiliations")
  public ResponseEntity<List<ExternalUserAffiliationDTO>> getAffiliationsOfExternalUser(
      @PathVariable UUID externalUserId) {
    log.debug("Getting affiliations for external user: {}", externalUserId);

    List<ExternalUserAffiliationDTO> affiliations =
        externalUserAffiliationService.getAffiliations(externalUserId).stream()
            .map(ExternalUserAffiliationApplicationMapper::toExternalUserAffiliationDTO)
            .toList();

    return ResponseEntity.ok(affiliations);
  }
}
