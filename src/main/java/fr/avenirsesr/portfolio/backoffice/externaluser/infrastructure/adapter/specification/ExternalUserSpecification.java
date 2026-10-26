package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

  public static Specification<ExternalUserEntity> withNameContaining(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return Specification.unrestricted();
    }
    String pattern = "%" + keyword.trim().toLowerCase() + "%";
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern));
  }

  public static Specification<ExternalUserEntity> withEmailContaining(String keyword) {
    return (root, query, criteriaBuilder) -> {
      if (keyword == null || keyword.isBlank()) {
        return null;
      }

      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get("email")), containsPattern(keyword), '\\');
    };
  }

  public static Specification<ExternalUserEntity> withNameOrEmailContaining(String keyword) {
    return Specification.where(withNameContaining(keyword)).or(withEmailContaining(keyword));
  }

  public static Specification<ExternalUserEntity> affiliatedMatching(
      Collection<EUserCategory> categories,
      Collection<UUID> institutionIds,
      Collection<UUID> groupIds) {
    return (root, query, criteriaBuilder) -> {
      Subquery<UUID> subquery = query.subquery(UUID.class);
      Root<ExternalUserAffiliationEntity> affiliationRoot =
          subquery.from(ExternalUserAffiliationEntity.class);
      subquery.select(affiliationRoot.get("id"));

      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.equal(affiliationRoot.get("externalUser"), root));
      if (!isEmpty(categories)) {
        predicates.add(affiliationRoot.get("category").in(categories));
      }
      if (!isEmpty(institutionIds)) {
        predicates.add(affiliationRoot.get("institution").get("id").in(institutionIds));
      }
      if (!isEmpty(groupIds)) {
        predicates.add(affiliationRoot.get("group").get("id").in(groupIds));
      }
      subquery.where(predicates.toArray(Predicate[]::new));

      return criteriaBuilder.exists(subquery);
    };
  }

  private static boolean isEmpty(Collection<?> values) {
    return values == null || values.isEmpty();
  }

  private static String containsPattern(String keyword) {
    String escaped =
        keyword.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    return "%" + escaped + "%";
  }
}
