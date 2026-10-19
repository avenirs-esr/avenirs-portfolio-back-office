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

  /**
   * Counts, for each requested group, the users affiliated to it or to any of its descendants.
   *
   * <p>The {@code subTrees} CTE walks the group hierarchy down from each requested group and pairs
   * it with every node of its sub-tree, itself included. Counting distinct users over those pairs
   * dedupes a user affiliated to several groups of the same sub-tree, which a count per group then
   * summed would double.
   *
   * <p>The depth cap only stops the recursion should a {@code parent_id} chain ever cycle, as
   * nothing in the schema forbids it. It sits well above the three levels the group types allow, so
   * it never truncates a legitimate hierarchy, and distinct counting makes the nodes a cycle
   * revisits harmless anyway.
   */
  @Query(
      "with subTrees as ("
          + "   select root.id as rootId, root.id as nodeId, 0 as depth"
          + "   from GroupEntity root"
          + "   where root.id in :groupIds"
          + " union all"
          + "   select parent.rootId as rootId, child.id as nodeId, parent.depth + 1 as depth"
          + "   from subTrees parent"
          + "   join GroupEntity child on child.parent.id = parent.nodeId"
          + "   where parent.depth < 10"
          + " )"
          + " select subTree.rootId as groupId, count(distinct a.externalUser.id) as usersCount"
          + " from subTrees subTree"
          + " join ExternalUserAffiliationEntity a on a.group.id = subTree.nodeId"
          + " group by subTree.rootId")
  List<GroupUsersCount> countAllByGroupIdsWithSubGroups(
      @Param("groupIds") Collection<UUID> groupIds);

  interface GroupUsersCount {
    UUID getGroupId();

    long getUsersCount();
  }
}
