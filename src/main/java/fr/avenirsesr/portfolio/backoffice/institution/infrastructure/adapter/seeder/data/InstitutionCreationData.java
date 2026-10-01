package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;

public record InstitutionCreationData(
    String name, String uai, String siret, EInstitutionType type, String parentUAI) {}
