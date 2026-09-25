package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.time.Instant;
import java.util.UUID;

public record ExternalUserAffiliationDTO(
    UUID id,
    UUID externalUserId,
    UUID institutionId,
    UUID groupId,
    EUserCategory category,
    Instant createdAt) {}
