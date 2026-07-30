package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import java.time.LocalDate;

/** CSV/JSON fixture shape for {@link GroupCreationData}, referencing the institution by hai. */
public record GroupCsvCreationData(
    String name,
    String idSiSco,
    String institutionHai,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSiSco) {}
