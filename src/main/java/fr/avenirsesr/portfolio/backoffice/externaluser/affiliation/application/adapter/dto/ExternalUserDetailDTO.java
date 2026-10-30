package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupReferenceDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionReferenceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"id", "firstName", "lastName", "email", "institution", "groups"})
public record ExternalUserDetailDTO(
    UUID id,
    String firstName,
    String lastName,
    String email,
    UUID userId,
    UUID profilePictureId,
    InstitutionReferenceDTO institution,
    List<GroupReferenceDTO> groups) {}
