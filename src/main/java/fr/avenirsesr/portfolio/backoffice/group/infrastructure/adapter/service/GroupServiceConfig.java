package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.service.GroupServiceImpl;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class GroupServiceConfig {
  private final GroupRepository groupRepository;
  private final InstitutionRepository institutionRepository;

  @Bean
  public GroupService groupService() {
    return new GroupServiceImpl(groupRepository, institutionRepository);
  }
}
