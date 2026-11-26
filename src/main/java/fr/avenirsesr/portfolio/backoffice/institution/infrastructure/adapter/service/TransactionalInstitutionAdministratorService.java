package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionAdministratorService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class TransactionalInstitutionAdministratorService
    implements InstitutionAdministratorService {

  private InstitutionAdministratorService delegate;

  @Override
  @Transactional
  public List<Institution> designate(UUID userId, List<UUID> institutionIds) {
    return delegate.designate(userId, institutionIds);
  }
}
