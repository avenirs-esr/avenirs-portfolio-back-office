package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    requiredProperties = {
      "id",
      "firstName",
      "lastName",
      "email",
      "hasConnected",
      "category",
      "institutionId",
      "institutionName"
    })
public record ExternalUserAffiliationOverviewDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    boolean hasConnected,
    UUID profilePictureId,
    @Schema(ref = "#/components/schemas/EUserCategory") EUserCategory category,
    UUID institutionId,
    String institutionName,
    UUID groupId,
    String groupName,
    @Schema(ref = "#/components/schemas/EGroupType") EGroupType groupType) {}
