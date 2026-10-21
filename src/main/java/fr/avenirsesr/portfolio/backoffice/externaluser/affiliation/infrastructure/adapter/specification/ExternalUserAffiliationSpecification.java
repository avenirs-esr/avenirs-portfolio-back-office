package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import jakarta.persistence.criteria.Path;
import java.util.Collection;
import java.util.List;
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

  public static Specification<ExternalUserAffiliationEntity> withCategory(EUserCategory category) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
  }

  public static Specification<ExternalUserAffiliationEntity> withCategories(
      List<EUserCategory> categories) {
    if (categories == null || categories.isEmpty()) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> root.get("category").in(categories);
  }

  public static Specification<ExternalUserAffiliationEntity> withInstitutionIds(
      Collection<UUID> institutionIds) {
    if (institutionIds == null || institutionIds.isEmpty()) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> root.get("institution").get("id").in(institutionIds);
  }

  public static Specification<ExternalUserAffiliationEntity> withGroupIds(
      Collection<UUID> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> root.get("group").get("id").in(groupIds);
  }

  public static Specification<ExternalUserAffiliationEntity> withExternalUserNameContaining(
      String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return Specification.unrestricted();
    }
    String pattern = "%" + keyword.trim().toLowerCase() + "%";
    return (root, query, criteriaBuilder) -> {
      Path<?> externalUser = root.get("externalUser");
      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(externalUser.get("firstName")), pattern),
          criteriaBuilder.like(criteriaBuilder.lower(externalUser.get("lastName")), pattern));
    };
  }
}
