package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import java.util.List;
import java.util.UUID;

public record StaffAccessCheckRequest(
    String eppn, List<UUID> targetInstitutionIds, List<UUID> targetGroupIds) {}
