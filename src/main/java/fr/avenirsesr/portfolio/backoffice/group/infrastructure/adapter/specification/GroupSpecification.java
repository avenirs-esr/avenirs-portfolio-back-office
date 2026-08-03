package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class GroupSpecification {
  public static Specification<GroupEntity> withInstitutionId(UUID institutionId) {
    if (institutionId == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("institution").get("id"), institutionId);
  }

  public static Specification<GroupEntity> withParentId(UUID parentId) {
    if (parentId == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("parent").get("id"), parentId);
  }

  public static Specification<GroupEntity> withType(EGroupType type) {
    if (type == null) {
      return null;
    }
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
  }

  public static Specification<GroupEntity> withStartDate(LocalDate startDate) {
    if (startDate == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate);
  }

  public static Specification<GroupEntity> withEndDate(LocalDate endDate) {
    if (endDate == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate);
  }
}
