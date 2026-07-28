package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionParentMustBePrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionPrimaryCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionSecondaryRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportFailure;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {
  private final InstitutionRepository institutionRepository;

  @Override
  public Institution create(
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      String parentHai) {
    return upsert(name, hai, siret, siren, type, parentHai).institution();
  }

  @Override
  public InstitutionImportSummary createAll(List<InstitutionData> institutions) {
    List<Institution> created = new ArrayList<>();
    List<Institution> updated = new ArrayList<>();
    List<InstitutionImportFailure> failed = new ArrayList<>();

    for (InstitutionData data : institutions) {
      try {
        UpsertResult result =
            upsert(
                data.name(), data.hai(), data.siret(), data.siren(), data.type(), data.parentHai());
        if (result.created()) {
          created.add(result.institution());
        } else {
          updated.add(result.institution());
        }
      } catch (BusinessException e) {
        log.warn("Failed to import institution with hai {}: {}", data.hai(), e.getMessage());
        failed.add(new InstitutionImportFailure(data.hai(), e.getMessage()));
      }
    }

    return new InstitutionImportSummary(created, updated, failed);
  }

  /** Creates the institution, or updates the existing one matching the given hai. */
  private UpsertResult upsert(
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      String parentHai) {
    Optional<Institution> existing = institutionRepository.findByHai(hai);
    if (existing.isPresent()) {
      return new UpsertResult(update(hai, name, siret, siren, type, parentHai), false);
    }

    Institution parent = resolveParent(type, parentHai);
    Institution institution =
        Institution.create(UUID.randomUUID(), name, hai, siret, siren, type, parent);
    return new UpsertResult(institutionRepository.save(institution), true);
  }

  private record UpsertResult(Institution institution, boolean created) {}

  @Override
  public Institution update(
      String hai,
      String name,
      String siret,
      String siren,
      EInstitutionType type,
      String parentHai) {
    Institution institution =
        institutionRepository.findByHai(hai).orElseThrow(InstitutionNotFoundException::new);

    Institution parent = resolveParent(type, parentHai);

    institution.setName(name);
    institution.setSiret(siret);
    institution.setSiren(siren);
    institution.setType(type);
    institution.setParent(parent);

    return institutionRepository.save(institution);
  }

  @Override
  public List<Institution> updateAll(List<InstitutionData> institutions) {
    return institutions.stream()
        .map(
            data ->
                update(
                    data.hai(),
                    data.name(),
                    data.siret(),
                    data.siren(),
                    data.type(),
                    data.parentHai()))
        .toList();
  }

  @Override
  public List<Institution> findAll() {
    return institutionRepository.findAll();
  }

  @Override
  public Institution findById(UUID id) {
    return institutionRepository.findById(id).orElseThrow(InstitutionNotFoundException::new);
  }

  @Override
  public void delete(UUID id) {
    institutionRepository.removeFromDatabase(findById(id));
  }

  private Institution resolveParent(EInstitutionType type, String parentHai) {
    if (type == EInstitutionType.PRIMARY) {
      if (parentHai != null) {
        throw new InstitutionPrimaryCannotHaveParentException();
      }
      return null;
    }

    if (parentHai == null) {
      throw new InstitutionSecondaryRequiresParentException();
    }

    Institution parent =
        institutionRepository.findByHai(parentHai).orElseThrow(InstitutionNotFoundException::new);

    if (parent.getType() != EInstitutionType.PRIMARY) {
      throw new InstitutionParentMustBePrimaryException();
    }

    return parent;
  }
}
