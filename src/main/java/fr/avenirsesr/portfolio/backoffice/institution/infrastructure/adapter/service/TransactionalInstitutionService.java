package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
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
      String uai,
      String siret,
      EInstitutionType type,
      String parentUAI) {
    return delegate.create(name, uai, siret, type, parentUAI);
  }

  @Override
  @Transactional
  public InstitutionImportSummary createAll(List<InstitutionData> institutions) {
    return delegate.createAll(institutions);
  }

  @Override
  public Institution update(
      String uai,
      String name,
      String siret,
      EInstitutionType type,
      String parentUAI) {
    return delegate.update(uai, name, siret, type, parentUAI);
  }

  @Override
  @Transactional
  public List<Institution> updateAll(List<InstitutionData> institutions) {
    return delegate.updateAll(institutions);
  }

  @Override
  public List<Institution> findAll(UUID parentId, EInstitutionType type) {
    return delegate.findAll(parentId, type);
  }

  @Override
  public Institution findById(UUID id) {
    return delegate.findById(id);
  }

  @Override
  public boolean staffHasAccess(List<UUID> affiliatedIds, List<UUID> targetIds) {
    return delegate.staffHasAccess(affiliatedIds, targetIds);
  }

  @Override
  public List<UUID> studentAccessibleIds(List<UUID> affiliatedIds) {
    return delegate.studentAccessibleIds(affiliatedIds);
  }

  @Override
  public void delete(UUID id) {
    delegate.delete(id);
  }
}
