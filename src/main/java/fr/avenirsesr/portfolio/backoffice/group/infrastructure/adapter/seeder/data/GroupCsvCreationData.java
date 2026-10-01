package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;

/** CSV/JSON fixture shape for {@link GroupCreationData}, referencing the institution by uai. */
public record GroupCsvCreationData(
    String name,
    String idSISco,
    String institutionUAI,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSISco) {}
