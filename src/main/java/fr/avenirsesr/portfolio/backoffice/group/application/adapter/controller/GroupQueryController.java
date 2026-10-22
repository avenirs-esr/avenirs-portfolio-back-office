package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper.GroupApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupOverviewDTO;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
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
 * the institutions by {@link GroupProvisioningController}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/groups")
public class GroupQueryController {

  private final GroupService groupService;
  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final LoggedInExternalUserService loggedInExternalUserService;

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping
  public ResponseEntity<PagedResponse<GroupOverviewDTO>> getGroups(
      @RequestParam(required = false) String keyword,
      @Parameter(array = @ArraySchema(schema = @Schema(ref = "#/components/schemas/EGroupType")))
          @RequestParam(required = false)
          List<EGroupType> types,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Getting the groups of the logged-in staff institution (keyword: {}, types: {}) {}",
        keyword,
        types,
        pageCriteria);

    PagedResult<Group> groups =
        groupService.findAllOfStaffInstitution(keyword, types, pageCriteria);
    List<Group> content = groups.content();
    Map<UUID, Integer> usersCounts =
        externalUserAffiliationService.countExternalUsersByGroupIds(
            content.stream().map(Group::getId).toList());

    return ResponseEntity.ok(
        new PagedResponse<>(
            content.stream()
                .map(
                    group ->
                        GroupApplicationMapper.toGroupOverviewDTO(
                            group, usersCounts.getOrDefault(group.getId(), 0)))
                .toList(),
            PageInfoDTO.fromDomain(groups.pageInfo())));
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}")
  public ResponseEntity<GroupDTO> getGroupById(@PathVariable UUID id) {
    log.debug("Getting group for id: {}", id);

    Group group = groupService.findById(id);
    Map<UUID, Integer> usersCounts =
        externalUserAffiliationService.countExternalUsersByGroupIds(List.of(group.getId()));
    return ResponseEntity.ok(
        GroupApplicationMapper.toGroupDTO(group, usersCounts.getOrDefault(group.getId(), 0)));
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}/sub-groups")
  public ResponseEntity<List<GroupOverviewDTO>> getSubGroupsOfGroup(@PathVariable UUID id) {
    log.debug("Getting sub-groups of group for id: {}", id);

    requireAffiliationToInstitutionOf(groupService.findById(id));

    List<Group> subGroups = groupService.findSubGroupsOf(id);
    Map<UUID, Integer> usersCounts =
        externalUserAffiliationService.countExternalUsersByGroupIds(
            subGroups.stream().map(Group::getId).toList());

    return ResponseEntity.ok(
        subGroups.stream()
            .map(
                subGroup ->
                    GroupApplicationMapper.toGroupOverviewDTO(
                        subGroup, usersCounts.getOrDefault(subGroup.getId(), 0)))
            .toList());
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}/program")
  public ResponseEntity<GroupDTO> getProgramOfGroup(@PathVariable UUID id) {
    log.debug("Getting program of group for id: {}", id);

    Group program = groupService.findProgramOf(id);
    Map<UUID, Integer> usersCounts =
        externalUserAffiliationService.countExternalUsersByGroupIds(List.of(program.getId()));

    return ResponseEntity.ok(
        GroupApplicationMapper.toGroupDTO(program, usersCounts.getOrDefault(program.getId(), 0)));
  }

  private void requireAffiliationToInstitutionOf(Group group) {
    ExternalUser loggedInUser = loggedInExternalUserService.getLoggedInExternalUser();
    UUID institutionId = group.getInstitution().getId();

    if (!externalUserAffiliationService.isAffiliated(loggedInUser, institutionId)) {
      log.warn(
          "User {} is not affiliated to the institution {} holding the group {}",
          loggedInUser.getId(),
          institutionId,
          group.getId());
      throw new UserNotAuthorizedException(
          "The group does not belong to an institution the user is affiliated to");
    }
  }
}
