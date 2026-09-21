package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.seeder.data;

import java.util.List;

public record ExternalUserAffiliationCreationData(
    String eppn, List<String> institutionHais, List<String> groupIdSiScos) {}
