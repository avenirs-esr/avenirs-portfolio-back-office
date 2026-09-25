package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import java.util.List;
import java.util.UUID;

public record ExternalUserAffiliationScope(List<UUID> institutionIds, List<UUID> groupIds) {}
