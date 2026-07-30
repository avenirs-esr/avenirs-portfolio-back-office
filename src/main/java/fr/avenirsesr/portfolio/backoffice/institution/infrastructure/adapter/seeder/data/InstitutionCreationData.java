package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;

public record InstitutionCreationData(
    String name, String hai, String siret, String siren, EInstitutionType type, String parentHai) {}
