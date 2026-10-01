package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionParentMustBePrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionPrimaryCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionSecondaryRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportFailure;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
      String uai,
      String siret,
      EInstitutionType type,
      String parentUAI) {
    return upsert(name, uai, siret, type, parentUAI).institution();
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
                data.name(), data.uai(), data.siret(), data.type(), data.parentUAI());
        if (result.created()) {
          created.add(result.institution());
        } else {
          updated.add(result.institution());
        }
      } catch (BusinessException e) {
        log.warn("Failed to import institution with uai {}: {}", data.uai(), e.getMessage());
        failed.add(new InstitutionImportFailure(data.uai(), e.getMessage()));
      }
    }

    return new InstitutionImportSummary(created, updated, failed);
  }

  /** Creates the institution, or updates the existing one matching the given uai. */
  private UpsertResult upsert(
      String name,
      String uai,
      String siret,
      EInstitutionType type,
      String parentUAI) {
    Optional<Institution> existing = institutionRepository.findByUai(uai);
    if (existing.isPresent()) {
      return new UpsertResult(update(uai, name, siret, type, parentUAI), false);
    }

    Institution parent = resolveParent(type, parentUAI);
    Institution institution =
        Institution.create(idFromUai(uai), name, uai, siret, type, parent);
    return new UpsertResult(institutionRepository.save(institution), true);
  }

  private static UUID idFromUai(String uai) {
    return UUID.nameUUIDFromBytes(("institution:" + uai).getBytes(StandardCharsets.UTF_8));
  }

  private record UpsertResult(Institution institution, boolean created) {}

  @Override
  public Institution update(
      String uai,
      String name,
      String siret,
      EInstitutionType type,
      String parentUAI) {
    Institution institution =
        institutionRepository.findByUai(uai).orElseThrow(InstitutionNotFoundException::new);

    Institution parent = resolveParent(type, parentUAI);

    institution.setName(name);
    institution.setSiret(siret);
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
                    data.uai(),
                    data.name(),
                    data.siret(),
                    data.type(),
                    data.parentUAI()))
        .toList();
  }

  @Override
  public List<Institution> findAll(UUID parentId, EInstitutionType type) {
    return institutionRepository.findAll(parentId, type);
  }

  @Override
  public Institution findById(UUID id) {
    return institutionRepository.findById(id).orElseThrow(InstitutionNotFoundException::new);
  }

  @Override
  public boolean staffHasAccess(List<UUID> affiliatedIds, List<UUID> targetIds) {
    List<UUID> distinctTargetIds = targetIds.stream().distinct().toList();
    List<Institution> targets = institutionRepository.findAllById(distinctTargetIds);
    if (targets.size() != distinctTargetIds.size()) {
      throw new InstitutionNotFoundException();
    }

    Set<UUID> affiliatedIdSet = new HashSet<>(affiliatedIds);
    return targets.stream().allMatch(target -> isCoveredByAffiliation(target, affiliatedIdSet));
  }

  private boolean isCoveredByAffiliation(Institution institution, Set<UUID> affiliatedIds) {
    Optional<Institution> current = Optional.of(institution);
    while (current.isPresent()) {
      if (affiliatedIds.contains(current.get().getId())) {
        return true;
      }
      current = current.get().getParent();
    }

    return false;
  }

  @Override
  public List<UUID> studentAccessibleIds(List<UUID> affiliatedIds) {
    List<UUID> distinctAffiliatedIds = affiliatedIds.stream().distinct().toList();
    List<Institution> affiliated = institutionRepository.findAllById(distinctAffiliatedIds);
    if (affiliated.size() != distinctAffiliatedIds.size()) {
      throw new InstitutionNotFoundException();
    }

    Set<UUID> accessibleIds = new LinkedHashSet<>();
    for (Institution institution : affiliated) {
      Optional<Institution> current = Optional.of(institution);
      while (current.isPresent() && accessibleIds.add(current.get().getId())) {
        current = current.get().getParent();
      }
    }

    return new ArrayList<>(accessibleIds);
  }

  @Override
  public void delete(UUID id) {
    institutionRepository.removeFromDatabase(findById(id));
  }

  private Institution resolveParent(EInstitutionType type, String parentUAI) {
    if (type == EInstitutionType.PRIMARY) {
      if (parentUAI != null) {
        throw new InstitutionPrimaryCannotHaveParentException();
      }
      return null;
    }

    if (parentUAI == null) {
      throw new InstitutionSecondaryRequiresParentException();
    }

    Institution parent =
        institutionRepository.findByUai(parentUAI).orElseThrow(InstitutionNotFoundException::new);

    if (parent.getType() != EInstitutionType.PRIMARY) {
      throw new InstitutionParentMustBePrimaryException();
    }

    return parent;
  }
}
