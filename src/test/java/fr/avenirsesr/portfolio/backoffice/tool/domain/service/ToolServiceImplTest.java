package fr.avenirsesr.portfolio.backoffice.tool.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.exception.ToolNotFoundException;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.FileContent;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.file.domain.port.input.FileResourceService;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToolServiceImplTest {
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String LEARNER_EPPN = "arthur.lambert@university.com";
  private static final String NAME = "Moodle";
  private static final String DESCRIPTION = "Plateforme de cours en ligne";
  private static final String URL = "https://moodle.example.fr";
  private static final String BANNER_NAME = "banner.png";
  private static final String PNG = "image/png";
  private static final long BANNER_SIZE = 1024L;
  private static final FileContent BANNER_CONTENT = FileContent.ofBytes(new byte[] {1, 2, 3});

  @Mock private ToolRepository toolRepository;
  @Mock private InstitutionRepository institutionRepository;
  @Mock private ExternalUserAffiliationService externalUserAffiliationService;
  @Mock private LoggedInExternalUserService loggedInExternalUserService;
  @Mock private FileResourceService fileResourceService;

  @InjectMocks private ToolServiceImpl service;

  private static Institution primaryInstitution() {
    return Institution.create(
        UUID.randomUUID(),
        "Université de Rennes",
        "UR",
        "0350001A",
        "13000550100015",
        EInstitutionType.PRIMARY,
        null);
  }

  private static Institution secondaryInstitution(Institution parent) {
    return Institution.create(
        UUID.randomUUID(),
        "Université de Rennes - IUT",
        "IUT-R",
        "0350002B",
        "13000550100023",
        EInstitutionType.SECONDARY,
        parent);
  }

  private static ExternalUser staff() {
    return ExternalUser.create(
        STAFF_EPPN,
        "STAFF-0001",
        EExternalSource.BACK_OFFICE,
        "0350001A",
        Set.of(EUserCategory.STAFF),
        STAFF_EPPN,
        "Ethan",
        "Perrin",
        EUserStatus.ACTIVE);
  }

  private static ExternalUser learner() {
    return ExternalUser.create(
        LEARNER_EPPN,
        "STUDENT-0001",
        EExternalSource.BACK_OFFICE,
        "0350002B",
        Set.of(EUserCategory.STUDENT),
        LEARNER_EPPN,
        "Arthur",
        "Lambert",
        EUserStatus.ACTIVE);
  }

  private void learnerScopeIs(List<UUID> institutionIds) {
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(learner());
    when(externalUserAffiliationService.studentScope(LEARNER_EPPN))
        .thenReturn(new ExternalUserAffiliationScope(institutionIds, List.of()));
  }

  private static File banner() {
    return File.create(
        UUID.randomUUID(),
        EFileType.PNG,
        "banner.png",
        1024,
        "banner.png",
        UUID.randomUUID(),
        false);
  }

  private Tool existingTool(File banner, Institution institution) {
    Tool tool = Tool.create(NAME, DESCRIPTION, URL, banner, institution);
    when(toolRepository.findById(tool.getId())).thenReturn(Optional.of(tool));
    return tool;
  }

  private void uploadingReturns(File uploaded) {
    when(fileResourceService.upload(
            eq(BANNER_NAME), eq(PNG), eq(BANNER_SIZE), any(FileContent.class), eq(false)))
        .thenReturn(uploaded);
  }

  private void savingReturnsTheTool() {
    when(toolRepository.save(any(Tool.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void staffHasAccessTo(Institution institution, boolean hasAccess) {
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff());
    when(externalUserAffiliationService.staffHasAccess(
            STAFF_EPPN, List.of(institution.getId()), List.of()))
        .thenReturn(hasAccess);
  }

  @Nested
  class WhenCreatingANationalTool {

    @Test
    void thenItShouldSaveATrimmedToolWithoutInstitution() {
      BddLogger.given("valid tool information surrounded by blanks");
      savingReturnsTheTool();

      BddLogger.when("creating a national tool");
      Tool tool = service.createNationalTool(" " + NAME + " ", DESCRIPTION + " ", " " + URL);

      BddLogger.then("it should save a national tool holding the trimmed information");
      assertThat(tool.getName()).isEqualTo(NAME);
      assertThat(tool.getDescription()).isEqualTo(DESCRIPTION);
      assertThat(tool.getUrl()).isEqualTo(URL);
      assertThat(tool.getInstitution()).isEmpty();
      assertThat(tool.getBanner()).isEmpty();
      assertThat(tool.getType()).isEqualTo(EToolType.NATIONAL);
      verify(toolRepository).save(tool);
    }

    @Test
    void thenItShouldNotCheckTheStaffScope() {
      BddLogger.given("valid tool information");
      savingReturnsTheTool();

      BddLogger.when("creating a national tool");
      service.createNationalTool(NAME, DESCRIPTION, URL);

      BddLogger.then("it should neither resolve the logged-in staff nor its affiliations");
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }

    @Test
    void thenItShouldRejectABlankName() {
      BddLogger.when("creating a national tool with a blank name");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.createNationalTool("  ", DESCRIPTION, URL));

      BddLogger.then("it should reject it as blank, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.NOT_BLANK);
      verifyNoInteractions(toolRepository);
    }

    @Test
    void thenItShouldRejectAMissingDescription() {
      BddLogger.when("creating a national tool without description");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class, () -> service.createNationalTool(NAME, null, URL));

      BddLogger.then("it should reject it as blank, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.NOT_BLANK);
      verifyNoInteractions(toolRepository);
    }

    @Test
    void thenItShouldAcceptADescriptionOfExactly200Characters() {
      BddLogger.given("a description of 200 characters, spaces included");
      savingReturnsTheTool();
      String description = "ab ".repeat(66) + "ab";

      BddLogger.when("creating a national tool");
      Tool tool = service.createNationalTool(NAME, description, URL);

      BddLogger.then("it should save it");
      assertThat(tool.getDescription()).hasSize(200).isEqualTo(description);
    }

    @Test
    void thenItShouldRejectADescriptionLongerThan200Characters() {
      BddLogger.given("a description of 201 characters");
      String description = "a".repeat(201);

      BddLogger.when("creating a national tool");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.createNationalTool(NAME, description, URL));

      BddLogger.then("it should reject it as too long, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.TOO_LONG);
      verifyNoInteractions(toolRepository);
    }

    @Test
    void thenItShouldRejectAMissingUrl() {
      BddLogger.when("creating a national tool without url");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.createNationalTool(NAME, DESCRIPTION, null));

      BddLogger.then("it should reject it as blank, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.NOT_BLANK);
      verifyNoInteractions(toolRepository);
    }

    @Test
    void thenItShouldRejectAnInvalidUrl() {
      BddLogger.when("creating a national tool with an url lacking its scheme");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.createNationalTool(NAME, DESCRIPTION, "moodle.example.fr"));

      BddLogger.then("it should reject it as not an url, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.NOT_URL);
      verifyNoInteractions(toolRepository);
    }
  }

  @Nested
  class WhenCreatingALocalTool {

    @Test
    void thenItShouldSaveARegionalToolForAnAccessiblePrimaryInstitution() {
      BddLogger.given("a primary institution accessible to the logged-in staff");
      Institution institution = primaryInstitution();
      when(institutionRepository.findById(institution.getId()))
          .thenReturn(Optional.of(institution));
      staffHasAccessTo(institution, true);
      savingReturnsTheTool();

      BddLogger.when("creating a tool for this institution");
      Tool tool = service.createLocalTool(NAME, DESCRIPTION, URL, institution.getId());

      BddLogger.then("it should save a regional tool attached to the institution");
      assertThat(tool.getInstitution()).contains(institution);
      assertThat(tool.getType()).isEqualTo(EToolType.REGIONAL);
      verify(toolRepository).save(tool);
    }

    @Test
    void thenItShouldSaveALocalToolForAnAccessibleSecondaryInstitution() {
      BddLogger.given("a secondary institution accessible to the logged-in staff");
      Institution institution = secondaryInstitution(primaryInstitution());
      when(institutionRepository.findById(institution.getId()))
          .thenReturn(Optional.of(institution));
      staffHasAccessTo(institution, true);
      savingReturnsTheTool();

      BddLogger.when("creating a tool for this institution");
      Tool tool = service.createLocalTool(NAME, DESCRIPTION, URL, institution.getId());

      BddLogger.then("it should save a local tool attached to the institution");
      assertThat(tool.getInstitution()).contains(institution);
      assertThat(tool.getType()).isEqualTo(EToolType.LOCAL);
    }

    @Test
    void thenItShouldRejectAnInstitutionOutsideTheStaffScope() {
      BddLogger.given("an institution the logged-in staff has no access to");
      Institution institution = primaryInstitution();
      when(institutionRepository.findById(institution.getId()))
          .thenReturn(Optional.of(institution));
      staffHasAccessTo(institution, false);

      BddLogger.when("creating a tool for this institution");
      assertThrows(
          UserNotAuthorizedException.class,
          () -> service.createLocalTool(NAME, DESCRIPTION, URL, institution.getId()));

      BddLogger.then("it should be refused, without saving anything");
      verify(toolRepository, never()).save(any());
    }

    @Test
    void thenItShouldRejectAnUnknownInstitution() {
      BddLogger.given("an institution id matching no institution");
      UUID institutionId = UUID.randomUUID();
      when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

      BddLogger.when("creating a tool for this institution");
      assertThrows(
          InstitutionNotFoundException.class,
          () -> service.createLocalTool(NAME, DESCRIPTION, URL, institutionId));

      BddLogger.then("it should report the institution as not found, without saving anything");
      verifyNoInteractions(toolRepository, externalUserAffiliationService);
    }

    @Test
    void thenItShouldRequireAnInstitution() {
      BddLogger.when("creating a tool without institution");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.createLocalTool(NAME, DESCRIPTION, URL, null));

      BddLogger.then("it should reject it as null, without saving anything");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.NOT_NULL);
      verifyNoInteractions(toolRepository, institutionRepository);
    }

    @Test
    void thenItShouldValidateTheFieldsBeforeResolvingTheInstitution() {
      BddLogger.when("creating a tool with a too long description");
      assertThrows(
          FieldValidationException.class,
          () -> service.createLocalTool(NAME, "a".repeat(201), URL, UUID.randomUUID()));

      BddLogger.then("it should be rejected before any lookup");
      verifyNoInteractions(toolRepository, institutionRepository, loggedInExternalUserService);
    }
  }

  @Nested
  class WhenUpdatingANationalTool {

    @Test
    void thenItShouldReplaceTheInformationWithTheTrimmedOnes() {
      BddLogger.given("an existing national tool with a banner");
      File banner = banner();
      Tool existing = existingTool(banner, null);
      savingReturnsTheTool();

      BddLogger.when("updating it with new information surrounded by blanks");
      Tool tool =
          service.updateNationalTool(
              existing.getId(), " Moodle 2 ", "Nouvelle description ", " https://new.example.fr");

      BddLogger.then("it should save the same tool holding the trimmed information");
      assertThat(tool.getId()).isEqualTo(existing.getId());
      assertThat(tool.getName()).isEqualTo("Moodle 2");
      assertThat(tool.getDescription()).isEqualTo("Nouvelle description");
      assertThat(tool.getUrl()).isEqualTo("https://new.example.fr");
      assertThat(tool.getType()).isEqualTo(EToolType.NATIONAL);
      assertThat(tool.getBanner()).contains(banner);
      verify(toolRepository).save(existing);
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }

    @Test
    void thenItShouldRefuseAToolAttachedToAnInstitution() {
      BddLogger.given("an existing regional tool");
      Tool existing = existingTool(null, primaryInstitution());

      BddLogger.when("updating it as a national tool");
      assertThrows(
          UserNotAuthorizedException.class,
          () -> service.updateNationalTool(existing.getId(), NAME, DESCRIPTION, URL));

      BddLogger.then("it should be refused, without saving anything");
      verify(toolRepository, never()).save(any());
    }

    @Test
    void thenItShouldRejectAnUnknownTool() {
      BddLogger.given("a tool id matching no tool");
      UUID toolId = UUID.randomUUID();
      when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

      BddLogger.when("updating it as a national tool");
      assertThrows(
          ToolNotFoundException.class,
          () -> service.updateNationalTool(toolId, NAME, DESCRIPTION, URL));

      BddLogger.then("it should report the tool as not found, without saving anything");
      verify(toolRepository, never()).save(any());
    }

    @Test
    void thenItShouldValidateTheFieldsBeforeResolvingTheTool() {
      BddLogger.when("updating a tool with a too long description");
      FieldValidationException exception =
          assertThrows(
              FieldValidationException.class,
              () -> service.updateNationalTool(UUID.randomUUID(), NAME, "a".repeat(201), URL));

      BddLogger.then("it should be rejected as too long, before any lookup");
      assertThat(exception.getErrorCode()).isEqualTo(EErrorCode.TOO_LONG);
      verifyNoInteractions(toolRepository);
    }
  }

  @Nested
  class WhenUpdatingALocalTool {

    @Test
    void thenItShouldReplaceTheInformationAndKeepTheInstitution() {
      BddLogger.given("an existing local tool of an institution accessible to the staff");
      Institution institution = secondaryInstitution(primaryInstitution());
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, true);
      savingReturnsTheTool();

      BddLogger.when("updating it");
      Tool tool =
          service.updateLocalTool(
              existing.getId(), "Moodle 2", "Nouvelle description", "https://new.example.fr");

      BddLogger.then("it should save the new information and keep its institution and type");
      assertThat(tool.getName()).isEqualTo("Moodle 2");
      assertThat(tool.getDescription()).isEqualTo("Nouvelle description");
      assertThat(tool.getUrl()).isEqualTo("https://new.example.fr");
      assertThat(tool.getInstitution()).contains(institution);
      assertThat(tool.getType()).isEqualTo(EToolType.LOCAL);
      verify(toolRepository).save(existing);
      verifyNoInteractions(institutionRepository);
    }

    @Test
    void thenItShouldRefuseANationalTool() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null, null);

      BddLogger.when("updating it as a local tool");
      assertThrows(
          UserNotAuthorizedException.class,
          () -> service.updateLocalTool(existing.getId(), NAME, DESCRIPTION, URL));

      BddLogger.then("it should be refused, without checking the staff scope nor saving");
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
      verify(toolRepository, never()).save(any());
    }

    @Test
    void thenItShouldRefuseAToolOutsideTheStaffScope() {
      BddLogger.given("an existing tool of an institution the staff has no access to");
      Institution institution = primaryInstitution();
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, false);

      BddLogger.when("updating it");
      assertThrows(
          UserNotAuthorizedException.class,
          () -> service.updateLocalTool(existing.getId(), NAME, DESCRIPTION, URL));

      BddLogger.then("it should be refused, without saving anything");
      verify(toolRepository, never()).save(any());
    }

    @Test
    void thenItShouldRejectAnUnknownTool() {
      BddLogger.given("a tool id matching no tool");
      UUID toolId = UUID.randomUUID();
      when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

      BddLogger.when("updating it as a local tool");
      assertThrows(
          ToolNotFoundException.class,
          () -> service.updateLocalTool(toolId, NAME, DESCRIPTION, URL));

      BddLogger.then("it should report the tool as not found, without checking the staff scope");
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }
  }

  @Nested
  class WhenManagingTheBannerOfANationalTool {

    @Test
    void thenItShouldUploadAnImageAsAPublicFileAndAttachIt() {
      BddLogger.given("an existing national tool without banner");
      Tool existing = existingTool(null, null);
      File uploaded = banner();
      uploadingReturns(uploaded);

      BddLogger.when("uploading a png banner");
      File result =
          service.uploadNationalToolBanner(
              existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT);

      BddLogger.then("it should store it unrestricted and attach it to the tool");
      assertThat(result).isEqualTo(uploaded);
      assertThat(existing.getBanner()).contains(uploaded);
      verify(toolRepository).save(existing);
      verify(fileResourceService, never()).delete(any());
    }

    @Test
    void thenItShouldDeleteThePreviousBannerOnceTheToolPointsToTheNewOne() {
      BddLogger.given("an existing national tool with a banner");
      File previous = banner();
      Tool existing = existingTool(previous, null);
      File uploaded = banner();
      uploadingReturns(uploaded);

      BddLogger.when("uploading another banner");
      service.uploadNationalToolBanner(
          existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT);

      BddLogger.then("it should save the tool with the new banner, then delete the previous one");
      InOrder order = inOrder(fileResourceService, toolRepository);
      order
          .verify(fileResourceService)
          .upload(eq(BANNER_NAME), eq(PNG), eq(BANNER_SIZE), any(FileContent.class), eq(false));
      order.verify(toolRepository).save(existing);
      order.verify(fileResourceService).delete(previous.getId());
      assertThat(existing.getBanner()).contains(uploaded);
    }

    @Test
    void thenItShouldRejectAFileThatIsNotAnImage() {
      BddLogger.when("uploading a pdf as the banner of a tool");
      assertThrows(
          FileTypeNotSupportedException.class,
          () ->
              service.uploadNationalToolBanner(
                  UUID.randomUUID(), "banner.pdf", "application/pdf", BANNER_SIZE, BANNER_CONTENT));

      BddLogger.then("it should be rejected before any lookup nor upload");
      verifyNoInteractions(toolRepository, fileResourceService);
    }

    @Test
    void thenItShouldRefuseTheBannerOfAToolAttachedToAnInstitution() {
      BddLogger.given("an existing regional tool");
      Tool existing = existingTool(null, primaryInstitution());

      BddLogger.when("uploading its banner as a national tool");
      assertThrows(
          UserNotAuthorizedException.class,
          () ->
              service.uploadNationalToolBanner(
                  existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT));

      BddLogger.then("it should be refused, without uploading anything");
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldDetachTheBannerBeforeDeletingIt() {
      BddLogger.given("an existing national tool with a banner");
      File banner = banner();
      Tool existing = existingTool(banner, null);

      BddLogger.when("deleting its banner");
      service.deleteNationalToolBanner(existing.getId());

      BddLogger.then("it should save the tool without banner, then delete the file");
      assertThat(existing.getBanner()).isEmpty();
      InOrder order = inOrder(toolRepository, fileResourceService);
      order.verify(toolRepository).save(existing);
      order.verify(fileResourceService).delete(banner.getId());
    }

    @Test
    void thenItShouldDoNothing_whenTheToolHasNoBanner() {
      BddLogger.given("an existing national tool without banner");
      Tool existing = existingTool(null, null);

      BddLogger.when("deleting its banner");
      service.deleteNationalToolBanner(existing.getId());

      BddLogger.then("it should neither save the tool nor delete any file");
      verify(toolRepository, never()).save(any());
      verifyNoInteractions(fileResourceService);
    }
  }

  @Nested
  class WhenManagingTheBannerOfALocalTool {

    @Test
    void thenItShouldUploadTheBannerOfAToolWithinTheStaffScope() {
      BddLogger.given("an existing local tool of an institution accessible to the staff");
      Institution institution = secondaryInstitution(primaryInstitution());
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, true);
      File uploaded = banner();
      uploadingReturns(uploaded);

      BddLogger.when("uploading its banner");
      File result =
          service.uploadLocalToolBanner(
              existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT);

      BddLogger.then("it should attach the uploaded banner to the tool");
      assertThat(result).isEqualTo(uploaded);
      assertThat(existing.getBanner()).contains(uploaded);
      verify(toolRepository).save(existing);
    }

    @Test
    void thenItShouldRefuseToUploadTheBannerOfAToolOutsideTheStaffScope() {
      BddLogger.given("an existing tool of an institution the staff has no access to");
      Institution institution = primaryInstitution();
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, false);

      BddLogger.when("uploading its banner");
      assertThrows(
          UserNotAuthorizedException.class,
          () ->
              service.uploadLocalToolBanner(
                  existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT));

      BddLogger.then("it should be refused, without uploading anything");
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldRefuseToUploadTheBannerOfANationalTool() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null, null);

      BddLogger.when("uploading its banner as a local tool");
      assertThrows(
          UserNotAuthorizedException.class,
          () ->
              service.uploadLocalToolBanner(
                  existing.getId(), BANNER_NAME, PNG, BANNER_SIZE, BANNER_CONTENT));

      BddLogger.then("it should be refused, without uploading anything");
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldDeleteTheBannerOfAToolWithinTheStaffScope() {
      BddLogger.given("an existing regional tool with a banner, accessible to the staff");
      Institution institution = primaryInstitution();
      File banner = banner();
      Tool existing = existingTool(banner, institution);
      staffHasAccessTo(institution, true);

      BddLogger.when("deleting its banner");
      service.deleteLocalToolBanner(existing.getId());

      BddLogger.then("it should detach the banner and delete the file");
      assertThat(existing.getBanner()).isEmpty();
      verify(toolRepository).save(existing);
      verify(fileResourceService).delete(banner.getId());
    }

    @Test
    void thenItShouldRefuseToDeleteTheBannerOfAToolOutsideTheStaffScope() {
      BddLogger.given("an existing tool with a banner, of an institution out of the staff scope");
      Institution institution = primaryInstitution();
      Tool existing = existingTool(banner(), institution);
      staffHasAccessTo(institution, false);

      BddLogger.when("deleting its banner");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.deleteLocalToolBanner(existing.getId()));

      BddLogger.then("it should be refused, keeping the banner");
      assertThat(existing.getBanner()).isPresent();
      verifyNoInteractions(fileResourceService);
      verify(toolRepository, never()).save(any());
    }
  }

  @Nested
  class WhenDeletingANationalTool {

    @Test
    void thenItShouldRemoveTheToolThenItsBanner() {
      BddLogger.given("an existing national tool with a banner");
      File banner = banner();
      Tool existing = existingTool(banner, null);

      BddLogger.when("deleting it");
      service.deleteNationalTool(existing.getId());

      BddLogger.then("it should remove the tool, then delete the file of its banner");
      InOrder order = inOrder(toolRepository, fileResourceService);
      order.verify(toolRepository).removeFromDatabase(existing);
      order.verify(fileResourceService).delete(banner.getId());
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }

    @Test
    void thenItShouldOnlyRemoveTheTool_whenItHasNoBanner() {
      BddLogger.given("an existing national tool without banner");
      Tool existing = existingTool(null, null);

      BddLogger.when("deleting it");
      service.deleteNationalTool(existing.getId());

      BddLogger.then("it should remove the tool without deleting any file");
      verify(toolRepository).removeFromDatabase(existing);
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldRefuseAToolAttachedToAnInstitution() {
      BddLogger.given("an existing regional tool");
      Tool existing = existingTool(banner(), primaryInstitution());

      BddLogger.when("deleting it as a national tool");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.deleteNationalTool(existing.getId()));

      BddLogger.then("it should be refused, removing nothing");
      verify(toolRepository, never()).removeFromDatabase(any());
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldRejectAnUnknownTool() {
      BddLogger.given("a tool id matching no tool");
      UUID toolId = UUID.randomUUID();
      when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

      BddLogger.when("deleting it as a national tool");
      assertThrows(ToolNotFoundException.class, () -> service.deleteNationalTool(toolId));

      BddLogger.then("it should report the tool as not found, removing nothing");
      verify(toolRepository, never()).removeFromDatabase(any());
    }
  }

  @Nested
  class WhenDeletingALocalTool {

    @Test
    void thenItShouldRemoveAToolWithinTheStaffScopeAndItsBanner() {
      BddLogger.given("an existing local tool with a banner, of an institution of the staff scope");
      Institution institution = secondaryInstitution(primaryInstitution());
      File banner = banner();
      Tool existing = existingTool(banner, institution);
      staffHasAccessTo(institution, true);

      BddLogger.when("deleting it");
      service.deleteLocalTool(existing.getId());

      BddLogger.then("it should remove the tool, then delete the file of its banner");
      InOrder order = inOrder(toolRepository, fileResourceService);
      order.verify(toolRepository).removeFromDatabase(existing);
      order.verify(fileResourceService).delete(banner.getId());
    }

    @Test
    void thenItShouldRefuseAToolOutsideTheStaffScope() {
      BddLogger.given("an existing tool of an institution the staff has no access to");
      Institution institution = primaryInstitution();
      Tool existing = existingTool(banner(), institution);
      staffHasAccessTo(institution, false);

      BddLogger.when("deleting it");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.deleteLocalTool(existing.getId()));

      BddLogger.then("it should be refused, removing nothing");
      verify(toolRepository, never()).removeFromDatabase(any());
      verifyNoInteractions(fileResourceService);
    }

    @Test
    void thenItShouldRefuseANationalTool() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null, null);

      BddLogger.when("deleting it as a local tool");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.deleteLocalTool(existing.getId()));

      BddLogger.then("it should be refused, without checking the staff scope nor removing");
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
      verify(toolRepository, never()).removeFromDatabase(any());
    }
  }

  @Nested
  class WhenListingTheNationalTools {

    @Test
    void thenItShouldReturnThePageOfNationalTools() {
      BddLogger.given("a page of national tools");
      PageCriteria pageCriteria = new PageCriteria(0, 8);
      PagedResult<Tool> page =
          new PagedResult<>(
              List.of(Tool.create(NAME, DESCRIPTION, URL, null, null)), new PageInfo(0, 8, 1));
      when(toolRepository.findAllNational(pageCriteria)).thenReturn(page);

      BddLogger.when("listing the national tools");
      PagedResult<Tool> result = service.findNationalTools(pageCriteria);

      BddLogger.then("it should return that page, without resolving any staff scope");
      assertThat(result).isSameAs(page);
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }
  }

  @Nested
  class WhenListingTheLocalTools {

    @Test
    void thenItShouldReturnTheToolsOfTheInstitutionsOfTheStaffScope() {
      BddLogger.given("a staff whose scope covers a primary institution and its secondary one");
      Institution primary = primaryInstitution();
      Institution secondary = secondaryInstitution(primary);
      List<UUID> scope = List.of(primary.getId(), secondary.getId());
      PageCriteria pageCriteria = new PageCriteria(1, 5);
      PagedResult<Tool> page =
          new PagedResult<>(
              List.of(Tool.create(NAME, DESCRIPTION, URL, null, secondary)), new PageInfo(1, 5, 6));
      when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff());
      when(externalUserAffiliationService.staffAccessibleInstitutionIds(STAFF_EPPN))
          .thenReturn(scope);
      when(toolRepository.findAllOfInstitutions(scope, EToolType.LOCAL, pageCriteria))
          .thenReturn(page);

      BddLogger.when("listing the local tools");
      PagedResult<Tool> result = service.findLocalTools(EToolType.LOCAL, pageCriteria);

      BddLogger.then("it should return the page of tools of the institutions of the staff scope");
      assertThat(result).isSameAs(page);
    }

    @Test
    void thenItShouldNotFilterOnTheType_whenNoneIsGiven() {
      BddLogger.given("a staff whose scope covers one institution");
      List<UUID> scope = List.of(primaryInstitution().getId());
      PageCriteria pageCriteria = new PageCriteria(0, 8);
      PagedResult<Tool> page = new PagedResult<>(List.of(), new PageInfo(0, 8, 0));
      when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff());
      when(externalUserAffiliationService.staffAccessibleInstitutionIds(STAFF_EPPN))
          .thenReturn(scope);
      when(toolRepository.findAllOfInstitutions(scope, null, pageCriteria)).thenReturn(page);

      BddLogger.when("listing the local tools without type");
      PagedResult<Tool> result = service.findLocalTools(null, pageCriteria);

      BddLogger.then("it should query the tools of the staff scope whatever their type");
      assertThat(result).isSameAs(page);
    }
  }

  @Nested
  class WhenGettingANationalTool {

    @Test
    void thenItShouldReturnTheNationalTool() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(banner(), null);

      BddLogger.when("getting it");
      Tool result = service.getNationalTool(existing.getId());

      BddLogger.then("it should return it, without resolving any staff scope");
      assertThat(result).isSameAs(existing);
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }

    @Test
    void thenItShouldRefuseAToolAttachedToAnInstitution() {
      BddLogger.given("an existing regional tool");
      Tool existing = existingTool(null, primaryInstitution());

      BddLogger.when("getting it as a national tool");

      BddLogger.then("it should be refused");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.getNationalTool(existing.getId()));
    }

    @Test
    void thenItShouldRejectAnUnknownTool() {
      BddLogger.given("a tool id matching no tool");
      UUID toolId = UUID.randomUUID();
      when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

      BddLogger.when("getting it as a national tool");

      BddLogger.then("it should report the tool as not found");
      assertThrows(ToolNotFoundException.class, () -> service.getNationalTool(toolId));
    }
  }

  @Nested
  class WhenGettingALocalTool {

    @Test
    void thenItShouldReturnAToolWithinTheStaffScope() {
      BddLogger.given("an existing local tool of an institution of the staff scope");
      Institution institution = secondaryInstitution(primaryInstitution());
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, true);

      BddLogger.when("getting it");
      Tool result = service.getLocalTool(existing.getId());

      BddLogger.then("it should return it");
      assertThat(result).isSameAs(existing);
    }

    @Test
    void thenItShouldRefuseAToolOutsideTheStaffScope() {
      BddLogger.given("an existing tool of an institution the staff has no access to");
      Institution institution = primaryInstitution();
      Tool existing = existingTool(null, institution);
      staffHasAccessTo(institution, false);

      BddLogger.when("getting it");

      BddLogger.then("it should be refused");
      assertThrows(UserNotAuthorizedException.class, () -> service.getLocalTool(existing.getId()));
    }

    @Test
    void thenItShouldRefuseANationalTool() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null, null);

      BddLogger.when("getting it as a local tool");
      assertThrows(UserNotAuthorizedException.class, () -> service.getLocalTool(existing.getId()));

      BddLogger.then("it should be refused, without checking the staff scope");
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }
  }

  @Nested
  class WhenListingTheAvailableTools {

    @Test
    void thenItShouldReturnTheNationalToolsAndThoseOfTheLearnerInstitutions() {
      BddLogger.given("a learner of a secondary institution, whose scope includes its primary one");
      Institution primary = primaryInstitution();
      Institution secondary = secondaryInstitution(primary);
      List<UUID> scope = List.of(secondary.getId(), primary.getId());
      learnerScopeIs(scope);
      PageCriteria pageCriteria = new PageCriteria(0, 8);
      PagedResult<Tool> page =
          new PagedResult<>(
              List.of(
                  Tool.create(NAME, DESCRIPTION, URL, null, null),
                  Tool.create(NAME, DESCRIPTION, URL, null, secondary)),
              new PageInfo(0, 8, 2));
      when(toolRepository.findAllNationalOrOfInstitutions(scope, null, pageCriteria))
          .thenReturn(page);

      BddLogger.when("listing the tools available to that learner");
      PagedResult<Tool> result = service.findAvailableTools(null, pageCriteria);

      BddLogger.then(
          "it should return the national tools and those of the institutions of the learner");
      assertThat(result).isSameAs(page);
    }

    @Test
    void thenItShouldFilterOnTheRequestedType() {
      BddLogger.given("a learner of a primary institution");
      List<UUID> scope = List.of(primaryInstitution().getId());
      learnerScopeIs(scope);
      PageCriteria pageCriteria = new PageCriteria(2, 4);
      PagedResult<Tool> page = new PagedResult<>(List.of(), new PageInfo(2, 4, 0));
      when(toolRepository.findAllNationalOrOfInstitutions(scope, EToolType.REGIONAL, pageCriteria))
          .thenReturn(page);

      BddLogger.when("listing the regional tools available to that learner");
      PagedResult<Tool> result = service.findAvailableTools(EToolType.REGIONAL, pageCriteria);

      BddLogger.then("it should only query the tools of the requested type");
      assertThat(result).isSameAs(page);
    }
  }

  @Nested
  class WhenGettingAnAvailableTool {

    @Test
    void thenItShouldReturnANationalTool_withoutResolvingTheLearnerScope() {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(banner(), null);

      BddLogger.when("getting it as a learner");
      Tool result = service.getAvailableTool(existing.getId());

      BddLogger.then("it should return it, every learner having access to the national tools");
      assertThat(result).isSameAs(existing);
      verifyNoInteractions(loggedInExternalUserService, externalUserAffiliationService);
    }

    @Test
    void thenItShouldReturnAToolOfAnInstitutionOfTheLearner() {
      BddLogger.given("an existing regional tool of the primary institution of the learner");
      Institution primary = primaryInstitution();
      Institution secondary = secondaryInstitution(primary);
      Tool existing = existingTool(null, primary);
      learnerScopeIs(List.of(secondary.getId(), primary.getId()));

      BddLogger.when("getting it as that learner");
      Tool result = service.getAvailableTool(existing.getId());

      BddLogger.then("it should return it");
      assertThat(result).isSameAs(existing);
    }

    @Test
    void thenItShouldRefuseAToolOfAnotherInstitution() {
      BddLogger.given("an existing local tool of an institution the learner does not belong to");
      Tool existing = existingTool(null, secondaryInstitution(primaryInstitution()));
      learnerScopeIs(List.of(UUID.randomUUID()));

      BddLogger.when("getting it as that learner");

      BddLogger.then("it should be refused");
      assertThrows(
          UserNotAuthorizedException.class, () -> service.getAvailableTool(existing.getId()));
    }

    @Test
    void thenItShouldRejectAnUnknownTool() {
      BddLogger.given("a tool id matching no tool");
      UUID toolId = UUID.randomUUID();
      when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

      BddLogger.when("getting it as a learner");

      BddLogger.then("it should report the tool as not found");
      assertThrows(ToolNotFoundException.class, () -> service.getAvailableTool(toolId));
    }
  }
}
