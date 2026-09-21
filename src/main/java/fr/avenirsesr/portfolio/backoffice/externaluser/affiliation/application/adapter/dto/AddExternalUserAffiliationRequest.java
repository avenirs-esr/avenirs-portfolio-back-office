package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import java.util.UUID;

public record AddExternalUserAffiliationRequest(UUID institutionId, UUID groupId) {}
