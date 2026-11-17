package fr.avenirsesr.portfolio.backoffice.tool.domain.service;

import static fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils.requireNotBlankAndMaxLength;
import static fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils.requireNotNull;
import static fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils.validateUrl;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ToolServiceImpl implements ToolService {
  private static final int NAME_MAX_LENGTH = 255;
  private static final int DESCRIPTION_MAX_LENGTH = 200;
  private static final int URL_MAX_LENGTH = 2048;

  private final ToolRepository toolRepository;
  private final InstitutionRepository institutionRepository;
  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final LoggedInExternalUserService loggedInExternalUserService;

  @Override
  public Tool createNationalTool(String name, String description, String url) {
    validateFields(name, description, url);

    Tool tool =
        toolRepository.save(Tool.create(name.trim(), description.trim(), url.trim(), null, null));
    log.info("National tool {} created", tool.getId());

    return tool;
  }

  @Override
  public Tool createLocalTool(String name, String description, String url, UUID institutionId) {
    validateFields(name, description, url);
    requireNotNull("institutionId", institutionId);

    Institution institution =
        institutionRepository
            .findById(institutionId)
            .orElseThrow(InstitutionNotFoundException::new);
    requireStaffAccess(institution);

    Tool tool =
        toolRepository.save(
            Tool.create(name.trim(), description.trim(), url.trim(), null, institution));
    log.info("{} tool {} created for institution {}", tool.getType(), tool.getId(), institutionId);

    return tool;
  }

  private static void validateFields(String name, String description, String url) {
    requireNotBlankAndMaxLength("name", name, NAME_MAX_LENGTH);
    requireNotBlankAndMaxLength("description", description, DESCRIPTION_MAX_LENGTH);
    requireNotBlankAndMaxLength("url", url, URL_MAX_LENGTH);
    validateUrl(url.trim());
  }

  private void requireStaffAccess(Institution institution) {
    ExternalUser staff = loggedInExternalUserService.getLoggedInExternalUser();
    if (!externalUserAffiliationService.staffHasAccess(
        staff.getEppn(), List.of(institution.getId()), List.of())) {
      log.warn(
          "Staff {} is not allowed to create a tool for institution {}",
          staff.getId(),
          institution.getId());
      throw new UserNotAuthorizedException(
          "The institution of the tool must be one the user is affiliated to");
    }
  }
}
