package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps the domain {@link InstitutionService} to add transaction boundaries around bulk operations,
 * keeping the Spring transaction dependency out of the domain layer.
 */
@AllArgsConstructor
public class TransactionalInstitutionService implements InstitutionService {
  private final InstitutionService delegate;

  @Override
  public Institution create(
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      String parentHai) {
    return delegate.create(name, hai, siret, siren, type, parentHai);
  }

  @Override
  @Transactional
  public InstitutionImportSummary createAll(List<InstitutionData> institutions) {
    return delegate.createAll(institutions);
  }

  @Override
  public Institution update(
      String hai,
      String name,
      String siret,
      String siren,
      EInstitutionType type,
      String parentHai) {
    return delegate.update(hai, name, siret, siren, type, parentHai);
  }

  @Override
  @Transactional
  public List<Institution> updateAll(List<InstitutionData> institutions) {
    return delegate.updateAll(institutions);
  }

  @Override
  public List<Institution> findAll() {
    return delegate.findAll();
  }

  @Override
  public Institution findById(UUID id) {
    return delegate.findById(id);
  }

  @Override
  public void delete(UUID id) {
    delegate.delete(id);
  }
}
