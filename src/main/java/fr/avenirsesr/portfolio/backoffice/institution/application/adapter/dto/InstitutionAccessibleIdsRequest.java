package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import java.util.List;
import java.util.UUID;

public record InstitutionAccessibleIdsRequest(List<UUID> affiliatedInstitutionIds) {}
