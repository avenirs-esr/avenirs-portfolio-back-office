package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data;

import java.util.UUID;

public record InstitutionConfigCreationData(
    UUID institutionId, boolean apcEnabled, boolean lifeProjectEnabled) {}
