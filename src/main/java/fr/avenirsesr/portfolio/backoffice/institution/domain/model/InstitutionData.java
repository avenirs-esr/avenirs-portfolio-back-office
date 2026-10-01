package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;

public record InstitutionData(
    String name, String sigle, String uai, String siret, EInstitutionType type, String parentUAI) {}
