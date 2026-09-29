package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import java.util.List;

public record StaffAffiliationScope(
    List<AffiliationScopeNode> institutions, List<AffiliationScopeNode> groups) {}
