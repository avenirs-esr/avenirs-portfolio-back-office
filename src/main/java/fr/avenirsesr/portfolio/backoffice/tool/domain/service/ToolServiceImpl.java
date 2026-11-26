package fr.avenirsesr.portfolio.backoffice.tool.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.exception.ToolNotFoundException;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.FileContent;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.file.domain.port.input.FileResourceService;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
import fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils;
import java.util.List;
import java.util.Optional;
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
  private final FileResourceService fileResourceService;

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
    FieldValidationUtils.requireNotNull("institutionId", institutionId);

    Institution institution =
        institutionRepository
            .findById(institutionId)
            .orElseThrow(InstitutionNotFoundException::new);
    requireStaffAccess(List.of(institutionId));

    Tool tool =
        toolRepository.save(
            Tool.create(name.trim(), description.trim(), url.trim(), null, institution));
    log.info("{} tool {} created for institution {}", tool.getType(), tool.getId(), institutionId);

    return tool;
  }

  @Override
  public Tool updateNationalTool(UUID toolId, String name, String description, String url) {
    validateFields(name, description, url);

    Tool updated =
        toolRepository.save(applyFields(findNationalTool(toolId), name, description, url));
    log.info("National tool {} updated", toolId);

    return updated;
  }

  @Override
  public Tool updateLocalTool(UUID toolId, String name, String description, String url) {
    validateFields(name, description, url);

    Tool updated = toolRepository.save(applyFields(findLocalTool(toolId), name, description, url));
    log.info("{} tool {} updated", updated.getType(), toolId);

    return updated;
  }

  @Override
  public File uploadNationalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content) {
    requireImage(mimeType);

    return replaceBanner(findNationalTool(toolId), fileName, mimeType, size, content);
  }

  @Override
  public File uploadLocalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content) {
    requireImage(mimeType);

    return replaceBanner(findLocalTool(toolId), fileName, mimeType, size, content);
  }

  @Override
  public void deleteNationalToolBanner(UUID toolId) {
    removeBanner(findNationalTool(toolId));
  }

  @Override
  public void deleteLocalToolBanner(UUID toolId) {
    removeBanner(findLocalTool(toolId));
  }

  @Override
  public void deleteNationalTools(List<UUID> toolIds) {
    List<Tool> tools = findTools(toolIds);
    tools.forEach(ToolServiceImpl::requireNational);

    removeTools(tools);
  }

  @Override
  public void deleteLocalTools(List<UUID> toolIds) {
    List<Tool> tools = findTools(toolIds);
    requireStaffAccess(
        tools.stream()
            .map(ToolServiceImpl::requireInstitution)
            .map(Institution::getId)
            .distinct()
            .toList());

    removeTools(tools);
  }

  @Override
  public PagedResult<Tool> findNationalTools(PageCriteria pageCriteria) {
    return toolRepository.findAllNational(pageCriteria);
  }

  @Override
  public PagedResult<Tool> findLocalTools(EToolType type, PageCriteria pageCriteria) {
    ExternalUser staff = loggedInExternalUserService.getLoggedInExternalUser();
    List<UUID> institutionIds =
        externalUserAffiliationService.staffAccessibleInstitutionIds(staff.getEppn());

    return toolRepository.findAllOfInstitutions(institutionIds, type, pageCriteria);
  }

  @Override
  public Tool getNationalTool(UUID toolId) {
    return findNationalTool(toolId);
  }

  @Override
  public Tool getLocalTool(UUID toolId) {
    return findLocalTool(toolId);
  }

  @Override
  public PagedResult<Tool> findAvailableTools(EToolType type, PageCriteria pageCriteria) {
    return toolRepository.findAllNationalOrOfInstitutions(
        learnerInstitutionIds(), type, pageCriteria);
  }

  @Override
  public Tool getAvailableTool(UUID toolId) {
    Tool tool = findTool(toolId);
    tool.getInstitution().ifPresent(this::requireLearnerAccess);

    return tool;
  }

  private void requireLearnerAccess(Institution institution) {
    if (!learnerInstitutionIds().contains(institution.getId())) {
      log.warn(
          "Tools of institution {} are not available to the logged-in learner",
          institution.getId());
      throw new UserNotAuthorizedException(
          "The tool must be national or belong to an institution of the learner");
    }
  }

  private List<UUID> learnerInstitutionIds() {
    ExternalUser learner = loggedInExternalUserService.getLoggedInExternalUser();
    return externalUserAffiliationService.studentScope(learner.getEppn()).institutionIds();
  }

  private void removeTools(List<Tool> tools) {
    toolRepository.removeAllFromDatabase(tools);
    tools.forEach(
        tool -> {
          tool.getBanner().ifPresent(banner -> fileResourceService.delete(banner.getId()));
          log.info("{} tool {} deleted", tool.getType(), tool.getId());
        });
  }

  private Tool findNationalTool(UUID toolId) {
    Tool tool = findTool(toolId);
    requireNational(tool);

    return tool;
  }

  private Tool findLocalTool(UUID toolId) {
    Tool tool = findTool(toolId);
    requireStaffAccess(List.of(requireInstitution(tool).getId()));

    return tool;
  }

  private Tool findTool(UUID toolId) {
    return toolRepository.findById(toolId).orElseThrow(ToolNotFoundException::new);
  }

  private List<Tool> findTools(List<UUID> toolIds) {
    FieldValidationUtils.requireNotNull("toolIds", toolIds);
    List<UUID> distinctIds = toolIds.stream().distinct().toList();
    List<Tool> tools = toolRepository.findAllById(distinctIds);
    if (tools.size() != distinctIds.size()) {
      log.warn("Some of the tools {} do not exist", distinctIds);
      throw new ToolNotFoundException();
    }

    return tools;
  }

  private static void requireNational(Tool tool) {
    if (tool.getType() != EToolType.NATIONAL) {
      log.warn(
          "Tool {} is {} and cannot be managed as a national tool", tool.getId(), tool.getType());
      throw new UserNotAuthorizedException("Only a national tool can be managed as such");
    }
  }

  private static Institution requireInstitution(Tool tool) {
    return tool.getInstitution()
        .orElseThrow(
            () -> {
              log.warn("Tool {} is national and cannot be managed as a local tool", tool.getId());
              return new UserNotAuthorizedException(
                  "A national tool cannot be managed as a local tool");
            });
  }

  private File replaceBanner(
      Tool tool, String fileName, String mimeType, long size, FileContent content) {
    Optional<File> previous = tool.getBanner();
    File banner = fileResourceService.upload(fileName, mimeType, size, content, false);

    tool.setBanner(banner);
    toolRepository.save(tool);
    previous.ifPresent(file -> fileResourceService.delete(file.getId()));
    log.info("Banner {} set on tool {}", banner.getId(), tool.getId());

    return banner;
  }

  private void removeBanner(Tool tool) {
    tool.getBanner()
        .ifPresent(
            banner -> {
              tool.setBanner(null);
              toolRepository.save(tool);
              fileResourceService.delete(banner.getId());
              log.info("Banner {} removed from tool {}", banner.getId(), tool.getId());
            });
  }

  private static Tool applyFields(Tool tool, String name, String description, String url) {
    tool.setName(name.trim());
    tool.setDescription(description.trim());
    tool.setUrl(url.trim());
    return tool;
  }

  private static void requireImage(String mimeType) {
    if (!EFileType.fromMimeType(mimeType).isImage()) {
      throw new FileTypeNotSupportedException("The banner of a tool must be an image");
    }
  }

  private static void validateFields(String name, String description, String url) {
    FieldValidationUtils.requireNotBlankAndMaxLength("name", name, NAME_MAX_LENGTH);
    FieldValidationUtils.requireNotBlankAndMaxLength(
        "description", description, DESCRIPTION_MAX_LENGTH);
    FieldValidationUtils.requireNotBlankAndMaxLength("url", url, URL_MAX_LENGTH);
    FieldValidationUtils.validateUrl(url.trim());
  }

  private void requireStaffAccess(List<UUID> institutionIds) {
    ExternalUser staff = loggedInExternalUserService.getLoggedInExternalUser();
    if (!externalUserAffiliationService.staffHasAccess(
        staff.getEppn(), institutionIds, List.of())) {
      log.warn(
          "Staff {} is not allowed to manage the tools of institutions {}",
          staff.getId(),
          institutionIds);
      throw new UserNotAuthorizedException(
          "The institution of the tool must be one the user is affiliated to");
    }
  }
}
