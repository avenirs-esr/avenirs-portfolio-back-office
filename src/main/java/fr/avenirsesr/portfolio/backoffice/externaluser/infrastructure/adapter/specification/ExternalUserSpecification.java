package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ExternalUserSpecification {
  public static Specification<ExternalUserEntity> withInstitutionId(UUID institutionId) {
    if (institutionId == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("institution").get("id"), institutionId);
  }

  public static Specification<ExternalUserEntity> withGroupId(UUID groupId) {
    if (groupId == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("group").get("id"), groupId);
  }
}
