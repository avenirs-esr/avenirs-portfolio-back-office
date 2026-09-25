package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.UUID;

public record AddExternalUserAffiliationRequest(
    UUID institutionId, UUID groupId, EUserCategory category) {}
