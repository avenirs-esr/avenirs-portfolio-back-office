package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalUserJpaRepository
    extends JpaRepository<ExternalUserEntity, UUID>, JpaSpecificationExecutor<ExternalUserEntity> {
  ExternalUserEntity findByEppn(String eppn);

  @Query(
      "select a.group.id as groupId, count(distinct a.externalUser.id) as usersCount"
          + " from ExternalUserAffiliationEntity a"
          + " where a.group.id in :groupIds"
          + " group by a.group.id")
  List<GroupUsersCount> countAllByGroupIds(@Param("groupIds") Collection<UUID> groupIds);

  interface GroupUsersCount {
    UUID getGroupId();

    long getUsersCount();
  }
}
