package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;

/** Seeder fixture shape, referencing the institution by uai. */
public record GroupCreationData(
    String name,
    String idSISco,
    String institutionUAI,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSISco) {}
