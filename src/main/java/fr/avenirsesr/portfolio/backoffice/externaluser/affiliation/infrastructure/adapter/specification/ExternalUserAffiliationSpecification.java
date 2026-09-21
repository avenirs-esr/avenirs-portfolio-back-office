package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ExternalUserAffiliationSpecification {

  public static Specification<ExternalUserAffiliationEntity> withExternalUserId(
      UUID externalUserId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("externalUser").get("id"), externalUserId);
  }

  public static Specification<ExternalUserAffiliationEntity> withInstitutionId(UUID institutionId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("institution").get("id"), institutionId);
  }

  public static Specification<ExternalUserAffiliationEntity> withGroupId(UUID groupId) {
    return (root, query, criteriaBuilder) ->
        groupId == null
            ? criteriaBuilder.isNull(root.get("group"))
            : criteriaBuilder.equal(root.get("group").get("id"), groupId);
  }
}
