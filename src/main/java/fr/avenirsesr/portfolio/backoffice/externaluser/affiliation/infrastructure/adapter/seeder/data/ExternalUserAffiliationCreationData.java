package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.List;

public record ExternalUserAffiliationCreationData(
    String eppn,
    EUserCategory category,
    List<String> institutionHais,
    List<String> groupIdSiScos) {}
