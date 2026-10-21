package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationOverviewDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper.ExternalUserAffiliationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/affiliations")
  public ResponseEntity<PagedResponse<ExternalUserAffiliationOverviewDTO>> getAffiliations(
      @RequestParam(required = false) String keyword,
      @Parameter(array = @ArraySchema(schema = @Schema(ref = "#/components/schemas/EUserCategory")))
          @RequestParam(required = false)
          List<EUserCategory> categories,
      @RequestParam(required = false) List<UUID> institutionIds,
      @RequestParam(required = false) List<UUID> groupIds,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Getting the affiliations (keyword: {}, categories: {}, institutions: {}, groups: {}) {}",
        keyword,
        categories,
        institutionIds,
        groupIds,
        pageCriteria);

    PagedResult<ExternalUserAffiliation> affiliations =
        externalUserAffiliationService.findAll(
            keyword, categories, institutionIds, groupIds, pageCriteria);

    return ResponseEntity.ok(
        new PagedResponse<>(
            affiliations.content().stream()
                .map(ExternalUserAffiliationApplicationMapper::toExternalUserAffiliationOverviewDTO)
                .toList(),
            PageInfoDTO.fromDomain(affiliations.pageInfo())));
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
