package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.Set;

public record ExternalUserCreationData(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EUserCategory> categories,
    String externalId,
    EExternalSource source,
    String institutionHai,
    String groupIdSiSco,
    EUserStatus status) {}
