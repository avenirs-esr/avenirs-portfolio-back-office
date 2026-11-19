package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model.ToolEntity;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import jakarta.persistence.criteria.JoinType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ToolSpecification {

  private ToolSpecification() {}

  public static Specification<ToolEntity> isNational() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("institution"));
  }

  public static Specification<ToolEntity> withInstitutionIds(Collection<UUID> institutionIds) {
    return (root, query, criteriaBuilder) ->
        institutionIds == null || institutionIds.isEmpty()
            ? criteriaBuilder.disjunction()
            : root.get("institution").get("id").in(institutionIds);
  }

  public static Specification<ToolEntity> withType(EToolType type) {
    if (type == null) {
      return Specification.unrestricted();
    }
    return switch (type) {
      case NATIONAL -> isNational();
      case REGIONAL -> withInstitutionType(EInstitutionType.PRIMARY);
      case LOCAL -> withInstitutionType(EInstitutionType.SECONDARY);
    };
  }

  private static Specification<ToolEntity> withInstitutionType(EInstitutionType institutionType) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("institution", JoinType.LEFT).get("type"), institutionType);
  }
}
