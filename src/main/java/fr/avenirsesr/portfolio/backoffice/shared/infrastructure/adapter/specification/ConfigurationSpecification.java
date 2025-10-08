package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.specification;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import org.springframework.data.jpa.domain.Specification;

public class ConfigurationSpecification {
  public static Specification<ConfigurationEntity> inScope(EConfigurationScope scope) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("scope"), scope);
  }
}
