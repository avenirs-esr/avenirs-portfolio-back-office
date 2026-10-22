package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupOverviewDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(
    requiredProperties = {
      "id",
      "firstName",
      "lastName",
      "email",
      "hasConnected",
      "categories",
      "institutions",
      "groups"
    })
public record ExternalUserAffiliationOverviewDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    boolean hasConnected,
    UUID profilePictureId,
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/EUserCategory"))
        List<EUserCategory> categories,
    List<InstitutionDTO> institutions,
    List<GroupOverviewDTO> groups) {}
