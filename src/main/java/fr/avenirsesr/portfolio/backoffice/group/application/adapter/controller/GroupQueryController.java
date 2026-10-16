package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupResponse;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper.GroupApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
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
  public ResponseEntity<List<GroupResponse>> getGroups(
      @RequestParam(required = false) UUID institutionId,
      @RequestParam(required = false) UUID parentId,
      @RequestParam(required = false) EGroupType type,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate) {
    log.debug(
        "Getting groups for institutionId: {}, parentId: {}, type: {}",
        institutionId,
        parentId,
        type);

    List<Group> groups = groupService.findAll(institutionId, parentId, type, startDate, endDate);
    return ResponseEntity.ok(groups.stream().map(GroupResponse::from).toList());
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
}
