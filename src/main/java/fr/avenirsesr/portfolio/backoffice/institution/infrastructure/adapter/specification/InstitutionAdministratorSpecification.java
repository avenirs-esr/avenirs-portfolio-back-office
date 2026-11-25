package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionAdministratorEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class InstitutionAdministratorSpecification {

  public static Specification<InstitutionAdministratorEntity> hasExternalUser(UUID externalUserId) {
    return (root, query, cb) -> cb.equal(root.get("externalUserId"), externalUserId);
  }

  public static Specification<InstitutionAdministratorEntity> hasInstitution(UUID institutionId) {
    return (root, query, cb) -> cb.equal(root.get("institution").get("id"), institutionId);
  }

  public static Specification<InstitutionAdministratorEntity> byUserAndInstitution(
      UUID externalUserId, UUID institutionId) {
    return Specification.where(hasExternalUser(externalUserId)).and(hasInstitution(institutionId));
  }
}
