package fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto;

import java.util.List;
import java.util.UUID;

public record GroupAccessibleIdsRequest(List<UUID> affiliatedGroupIds) {}
