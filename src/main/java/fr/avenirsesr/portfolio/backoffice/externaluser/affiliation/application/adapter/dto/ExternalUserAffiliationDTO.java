package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import java.time.Instant;
import java.util.UUID;

public record ExternalUserAffiliationDTO(
    UUID id, UUID externalUserId, UUID institutionId, UUID groupId, Instant createdAt) {}
