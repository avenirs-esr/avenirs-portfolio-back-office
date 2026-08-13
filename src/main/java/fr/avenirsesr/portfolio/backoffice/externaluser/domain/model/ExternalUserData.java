package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.Set;
import java.util.UUID;

public record ExternalUserData(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EUserCategory> categories,
    String externalId,
    EExternalSource source,
    UUID institutionId,
    UUID groupId,
    EUserStatus status) {}
