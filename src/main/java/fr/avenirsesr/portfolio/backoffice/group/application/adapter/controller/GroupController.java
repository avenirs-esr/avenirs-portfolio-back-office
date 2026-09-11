package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper.GroupApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/groups")
public class GroupController {

  private final GroupService groupService;

  @GetMapping("/{id}")
  public ResponseEntity<GroupDTO> getGroupById(@PathVariable UUID id) {
    log.debug("Getting group for id: {}", id);

    return ResponseEntity.ok(GroupApplicationMapper.toGroupDTO(groupService.findById(id)));
  }

  @GetMapping("/{id}/program")
  public ResponseEntity<GroupDTO> getProgramOfGroup(@PathVariable UUID id) {
    log.debug("Getting program of group for id: {}", id);

    return ResponseEntity.ok(GroupApplicationMapper.toGroupDTO(groupService.findProgramOf(id)));
  }
}
