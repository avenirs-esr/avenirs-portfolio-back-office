package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class GroupSpecification {
  public static Specification<GroupEntity> withInstitutionId(UUID institutionId) {
    if (institutionId == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("institution").get("id"), institutionId);
  }

  public static Specification<GroupEntity> withParentIds(Collection<UUID> parentIds) {
    return (root, query, criteriaBuilder) ->
        parentIds == null || parentIds.isEmpty()
            ? criteriaBuilder.disjunction()
            : root.get("parent").get("id").in(parentIds);
  }

  public static Specification<GroupEntity> withType(EGroupType type) {
    if (type == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
  }

  public static Specification<GroupEntity> withNameContaining(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return Specification.unrestricted();
    }
    String pattern = "%" + keyword.trim().toLowerCase() + "%";
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
  }
}
