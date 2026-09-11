package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;

public record InstitutionData(
    String name, String hai, String siret, String siren, EInstitutionType type, String parentHai) {}
