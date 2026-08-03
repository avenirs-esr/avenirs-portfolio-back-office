package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class InstitutionSpecification {
  public static Specification<InstitutionEntity> withParentId(UUID parentId) {
    if (parentId == null) {
      return null;
    }
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("parent").get("id"), parentId);
  }

  public static Specification<InstitutionEntity> withType(EInstitutionType type) {
    if (type == null) {
      return null;
    }
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
  }
}
