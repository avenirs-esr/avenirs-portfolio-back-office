package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception.ExternalUserAffiliationNotFoundException;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportFailure;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ExternalUserAffiliationServiceImpl implements ExternalUserAffiliationService {

  private final ExternalUserAffiliationRepository externalUserAffiliationRepository;
  private final ExternalUserRepository externalUserRepository;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Override
  public ExternalUserAffiliation addAffiliation(
      UUID externalUserId, UUID institutionId, UUID groupId) {
    ExternalUser externalUser =
        externalUserRepository
            .findById(externalUserId)
            .orElseThrow(ExternalUserNotFoundException::new);
    Institution institution =
        institutionRepository
            .findById(institutionId)
            .orElseThrow(InstitutionNotFoundException::new);
    Group group =
        groupId != null
            ? groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new)
            : null;

    return findOrCreate(externalUser, institution, group).affiliation();
  }

  @Override
  public void removeAffiliation(UUID externalUserId, UUID affiliationId) {
    ExternalUserAffiliation affiliation =
        externalUserAffiliationRepository
            .findById(affiliationId)
            .filter(a -> a.getExternalUser().getId().equals(externalUserId))
            .orElseThrow(ExternalUserAffiliationNotFoundException::new);

    externalUserAffiliationRepository.removeFromDatabase(affiliation);
  }

  @Override
  public List<ExternalUserAffiliation> getAffiliations(UUID externalUserId) {
    externalUserRepository.findById(externalUserId).orElseThrow(ExternalUserNotFoundException::new);

    return externalUserAffiliationRepository.findAllByExternalUserId(externalUserId);
  }

  @Override
  public ExternalUserAffiliationImportSummary createAll(
      List<ExternalUserAffiliationData> affiliations) {
    List<ExternalUserAffiliation> created = new ArrayList<>();
    List<ExternalUserAffiliation> existing = new ArrayList<>();
    List<ExternalUserAffiliationImportFailure> failed = new ArrayList<>();

    for (ExternalUserAffiliationData data : affiliations) {
      try {
        UpsertResult result = upsertByNaturalKeys(data);
        if (result.created()) {
          created.add(result.affiliation());
        } else {
          existing.add(result.affiliation());
        }
      } catch (BusinessException e) {
        log.warn(
            "Failed to import affiliation for eppn {} / institution {} / group {}: {}",
            data.eppn(),
            data.institutionHai(),
            data.groupIdSiSco(),
            e.getMessage());
        failed.add(
            new ExternalUserAffiliationImportFailure(
                data.eppn(), data.institutionHai(), data.groupIdSiSco(), e.getMessage()));
      }
    }

    return new ExternalUserAffiliationImportSummary(created, existing, failed);
  }

  /**
   * Resolves the external user, institution and group from their natural keys, then finds or
   * creates the affiliation.
   */
  private UpsertResult upsertByNaturalKeys(ExternalUserAffiliationData data) {
    ExternalUser externalUser =
        externalUserRepository
            .findByEppn(data.eppn())
            .orElseThrow(ExternalUserNotFoundException::new);
    Institution institution =
        institutionRepository
            .findByHai(data.institutionHai())
            .orElseThrow(InstitutionNotFoundException::new);
    Group group =
        data.groupIdSiSco() != null
            ? groupRepository
                .findByIdSiSco(data.groupIdSiSco())
                .orElseThrow(GroupNotFoundException::new)
            : null;

    return findOrCreate(externalUser, institution, group);
  }

  private UpsertResult findOrCreate(
      ExternalUser externalUser, Institution institution, Group group) {
    UUID groupId = group != null ? group.getId() : null;

    return externalUserAffiliationRepository
        .findByExternalUserIdAndInstitutionIdAndGroupId(
            externalUser.getId(), institution.getId(), groupId)
        .map(affiliation -> new UpsertResult(affiliation, false))
        .orElseGet(
            () ->
                new UpsertResult(
                    externalUserAffiliationRepository.save(
                        ExternalUserAffiliation.create(externalUser, institution, group)),
                    true));
  }

  private record UpsertResult(ExternalUserAffiliation affiliation, boolean created) {}
}
