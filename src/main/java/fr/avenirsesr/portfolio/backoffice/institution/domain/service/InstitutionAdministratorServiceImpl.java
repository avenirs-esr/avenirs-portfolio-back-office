package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.ExternalUserNotStaffException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionAdministratorRoleGrantException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotPrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionAdministratorService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleGranter;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class InstitutionAdministratorServiceImpl implements InstitutionAdministratorService {

  private final InstitutionAdministratorRepository institutionAdministratorRepository;
  private final InstitutionRepository institutionRepository;
  private final ExternalUserRepository externalUserRepository;
  private final LoggedInExternalUserService loggedInExternalUserService;
  private final InstitutionAdministratorRoleGranter roleGranter;

  @Override
  public List<Institution> designate(UUID userId, List<UUID> institutionIds) {
    FieldValidationUtils.requireNotNull("userId", userId);
    FieldValidationUtils.requireNotNull("institutionIds", institutionIds);

    if (institutionIds.isEmpty()) {
      throw new FieldValidationException(EErrorCode.INSTITUTION_ID_NULL);
    }

    List<UUID> distinctIds = institutionIds.stream().distinct().toList();

    ExternalUser user =
        externalUserRepository.findById(userId).orElseThrow(ExternalUserNotFoundException::new);

    requireStaff(user);

    List<UUID> existingInstitutionIds =
        institutionAdministratorRepository.findInstitutionIdsByUser(userId);

    boolean firstDesignation = existingInstitutionIds.isEmpty();

    List<Institution> institutions = resolvePrimaryInstitution(distinctIds);

    UUID createdBy = loggedInExternalUserService.getLoggedInExternalUser().getId();

    Set<UUID> assignedInstitutionIds = new HashSet<>(existingInstitutionIds);

    if (firstDesignation) {
      grantAdminRole(userId);
    }

    List<Institution> newlyDesignated =
        persistMissingDesignations(userId, institutions, createdBy, assignedInstitutionIds);

    List<UUID> administeredIds =
        Stream.concat(
                existingInstitutionIds.stream(), newlyDesignated.stream().map(Institution::getId))
            .distinct()
            .toList();
    return administeredInstitutions(administeredIds);
  }

  private void requireStaff(ExternalUser user) {
    if (!user.getCategories().contains(EUserCategory.STAFF)) {
      log.warn(
          "User {} is not STAFF and cannot be designated as institution administrator",
          user.getId());
      throw new ExternalUserNotStaffException();
    }
  }

  private List<Institution> resolvePrimaryInstitution(List<UUID> institutionIds) {
    List<Institution> institutions = institutionRepository.findAllById(institutionIds);

    if (institutions.size() != institutionIds.size()) {
      throw new InstitutionNotFoundException();
    }

    if (!institutions.stream()
        .allMatch(institution -> institution.getType() == EInstitutionType.PRIMARY)) {
      log.warn("Some institutions are not PRIMARY and cannot have an administrator");
      throw new InstitutionNotPrimaryException();
    }

    return institutions;
  }

  private List<Institution> persistMissingDesignations(
      UUID userId,
      List<Institution> institutions,
      UUID createdBy,
      Set<UUID> existingInstitutionIds) {
    List<Institution> toDesignate =
        institutions.stream()
            .filter(institution -> !existingInstitutionIds.contains(institution.getId()))
            .toList();

    toDesignate.forEach(
        institution ->
            institutionAdministratorRepository.save(
                InstitutionAdministrator.create(userId, institution, createdBy)));

    return toDesignate;
  }

  private void grantAdminRole(UUID userId) {
    try {
      roleGranter.grantAdminRole(userId);
      log.info("Granted the administrator role to user {}", userId);
    } catch (RuntimeException e) {
      log.error(
          "Failed to grant the administrator role to user {} removing the designation created in"
              + " this operation",
          userId,
          e);
      throw new InstitutionAdministratorRoleGrantException();
    }
  }

  private List<Institution> administeredInstitutions(List<UUID> institutionIds) {
    return institutionRepository.findAllById(institutionIds);
  }
}
