package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalUserAffiliationJpaRepository
    extends JpaRepository<ExternalUserAffiliationEntity, UUID>,
        JpaSpecificationExecutor<ExternalUserAffiliationEntity> {

  @Query(
      "select distinct a.institution.id from ExternalUserAffiliationEntity a"
          + " where a.externalUser.id = :externalUserId and a.category = :category")
  List<UUID> findDistinctInstitutionIds(
      @Param("externalUserId") UUID externalUserId, @Param("category") EUserCategory category);

  @Query(
      "select distinct a.group.id from ExternalUserAffiliationEntity a"
          + " where a.externalUser.id = :externalUserId and a.category = :category"
          + " and a.group.id is not null")
  List<UUID> findDistinctGroupIds(
      @Param("externalUserId") UUID externalUserId, @Param("category") EUserCategory category);
}
