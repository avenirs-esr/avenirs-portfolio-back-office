package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class InstitutionSpecification {
  public static Specification<InstitutionEntity> withParentId(UUID parentId) {
    if (parentId == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("parent").get("id"), parentId);
  }

  public static Specification<InstitutionEntity> withParentIds(Collection<UUID> parentIds) {
    return (root, query, criteriaBuilder) ->
        parentIds == null || parentIds.isEmpty()
            ? criteriaBuilder.disjunction()
            : root.get("parent").get("id").in(parentIds);
  }

  public static Specification<InstitutionEntity> withType(EInstitutionType type) {
    if (type == null) {
      return Specification.unrestricted();
    }
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
  }
}
