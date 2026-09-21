package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import jakarta.persistence.criteria.Subquery;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ExternalUserSpecification {
  public static Specification<ExternalUserEntity> withInstitutionId(UUID institutionId) {
    if (institutionId == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> {
      Subquery<UUID> subquery = query.subquery(UUID.class);
      var affiliationRoot = subquery.from(ExternalUserAffiliationEntity.class);
      subquery.select(affiliationRoot.get("id"));
      subquery.where(
          criteriaBuilder.equal(affiliationRoot.get("externalUser"), root),
          criteriaBuilder.equal(affiliationRoot.get("institution").get("id"), institutionId));
      return criteriaBuilder.exists(subquery);
    };
  }

  public static Specification<ExternalUserEntity> withGroupId(UUID groupId) {
    if (groupId == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> {
      Subquery<UUID> subquery = query.subquery(UUID.class);
      var affiliationRoot = subquery.from(ExternalUserAffiliationEntity.class);
      subquery.select(affiliationRoot.get("id"));
      subquery.where(
          criteriaBuilder.equal(affiliationRoot.get("externalUser"), root),
          criteriaBuilder.equal(affiliationRoot.get("group").get("id"), groupId));
      return criteriaBuilder.exists(subquery);
    };
  }
}
