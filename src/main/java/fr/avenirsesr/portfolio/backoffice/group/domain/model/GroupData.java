package fr.avenirsesr.portfolio.backoffice.group.domain.model;

import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;

public record GroupData(
    String name,
    String idSISco,
    String institutionUAI,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSISco) {}
