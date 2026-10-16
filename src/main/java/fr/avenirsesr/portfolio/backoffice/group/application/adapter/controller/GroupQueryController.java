package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper.GroupApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupOverviewDTO;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
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

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping
  public ResponseEntity<PagedResponse<GroupOverviewDTO>> getGroups(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) EGroupType type,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Getting the groups of the logged-in staff institution (keyword: {}, type: {}) {}",
        keyword,
        type,
        pageCriteria);

    PagedResult<Group> groups =
        groupService.findAllOfStaffPrimaryInstitution(keyword, type, pageCriteria);

    return ResponseEntity.ok(
        new PagedResponse<>(
            groups.content().stream().map(GroupQueryController::toOverview).toList(),
            PageInfoDTO.fromDomain(groups.pageInfo())));
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}")
  public ResponseEntity<GroupDTO> getGroupById(@PathVariable UUID id) {
    log.debug("Getting group for id: {}", id);

    return ResponseEntity.ok(GroupApplicationMapper.toGroupDTO(groupService.findById(id)));
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}/program")
  public ResponseEntity<GroupDTO> getProgramOfGroup(@PathVariable UUID id) {
    log.debug("Getting program of group for id: {}", id);

    return ResponseEntity.ok(GroupApplicationMapper.toGroupDTO(groupService.findProgramOf(id)));
  }

  /** The member count is not wired yet, it is reported as 0 until its source is settled. */
  private static GroupOverviewDTO toOverview(Group group) {
    return GroupApplicationMapper.toGroupOverviewDTO(group, 0);
  }
}
