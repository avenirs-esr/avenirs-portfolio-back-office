package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    requiredProperties = {
      "id",
      "externalUserId",
      "eppn",
      "firstName",
      "lastName",
      "email",
      "category",
      "institutionId",
      "institutionName"
    })
public record ExternalUserAffiliationOverviewDTO(
    UUID id,
    UUID externalUserId,
    String eppn,
    String firstName,
    String lastName,
    String email,
    @Schema(ref = "#/components/schemas/EUserCategory") EUserCategory category,
    UUID institutionId,
    String institutionName,
    UUID groupId,
    String groupName,
    @Schema(ref = "#/components/schemas/EGroupType") EGroupType groupType) {}
