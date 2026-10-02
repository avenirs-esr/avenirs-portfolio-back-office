package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import java.util.List;
import java.util.UUID;

public record AffiliationScopeNode(
    UUID id, String title, EAffiliationScopeNodeType type, List<AffiliationScopeNode> children) {}
