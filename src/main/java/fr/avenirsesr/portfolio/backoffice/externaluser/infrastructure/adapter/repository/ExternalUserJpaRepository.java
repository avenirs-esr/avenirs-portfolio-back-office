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
   * Walks the group hierarchy down from each requested group and pairs it with every node of its
   * sub-tree, itself included. Shared by the queries below, so that a group lists exactly the users
   * it counts.
   *
   * <p>The depth cap only stops the recursion should a {@code parent_id} chain ever cycle, as
   * nothing in the schema forbids it. It sits well above the three levels the group types allow, so
   * it never truncates a legitimate hierarchy, and the queries below dedupe the nodes a cycle
   * revisits.
   */
  String SUB_TREES_CTE =
      "with subTrees as ("
          + "   select root.id as rootId, root.id as nodeId, 0 as depth"
          + "   from GroupEntity root"
          + "   where root.id in :groupIds"
          + " union all"
          + "   select parent.rootId as rootId, child.id as nodeId, parent.depth + 1 as depth"
          + "   from subTrees parent"
          + "   join GroupEntity child on child.parent.id = parent.nodeId"
          + "   where parent.depth < 10"
          + " )";

  /**
   * Counts, for each requested group, the users affiliated to it or to any of its descendants.
   * Counting distinct users over the {@link #SUB_TREES_CTE} pairs dedupes a user affiliated to
   * several groups of the same sub-tree, which a count per group then summed would double.
   */
  @Query(
      SUB_TREES_CTE
          + " select subTree.rootId as groupId, count(distinct a.externalUser.id) as usersCount"
          + " from subTrees subTree"
          + " join ExternalUserAffiliationEntity a on a.group.id = subTree.nodeId"
          + " group by subTree.rootId")
  List<GroupUsersCount> countAllByGroupIdsWithSubGroups(
      @Param("groupIds") Collection<UUID> groupIds);

  /** Lists the requested groups and every group below them, the sub-trees counted above. */
  @Query(SUB_TREES_CTE + " select distinct subTree.nodeId from subTrees subTree")
  List<UUID> findAllGroupIdsWithSubGroups(@Param("groupIds") Collection<UUID> groupIds);

  interface GroupUsersCount {
    UUID getGroupId();

    long getUsersCount();
  }
}
