package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionHaiAlreadyExistsException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionParentMustBePrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionPrimaryCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionSecondaryRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
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
    if (institutionRepository.existsByHai(hai)) {
      throw new InstitutionHaiAlreadyExistsException();
    }

    Institution parent = resolveParent(type, parentHai);

    Institution institution =
        Institution.create(UUID.randomUUID(), name, hai, siret, siren, type, parent);
    return institutionRepository.save(institution);
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
