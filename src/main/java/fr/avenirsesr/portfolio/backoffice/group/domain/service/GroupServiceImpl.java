package fr.avenirsesr.portfolio.backoffice.group.domain.service;

import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupIdSiScoAlreadyExistsException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionParentMustBeProgramException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupParentMustBeProgramOrOptionException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {
  private final GroupRepository groupRepository;
  private final InstitutionRepository institutionRepository;

  @Override
  public Group create(
      String name,
      String idSiSco,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    if (groupRepository.existsByIdSiSco(idSiSco)) {
      throw new GroupIdSiScoAlreadyExistsException();
    }

    Institution institution = findInstitutionOrThrow(institutionId);
    Group parent = resolveParent(type, parentIdSiSco);

    Group group =
        Group.create(
            UUID.randomUUID(),
            name,
            idSiSco,
            institution,
            codeSise,
            startDate,
            endDate,
            type,
            parent);
    return groupRepository.save(group);
  }

  private Institution findInstitutionOrThrow(UUID institutionId) {
    return institutionRepository
        .findById(institutionId)
        .orElseThrow(InstitutionNotFoundException::new);
  }

  private Group resolveParent(EGroupType type, String parentIdSiSco) {
    return switch (type) {
      case PROGRAM -> {
        if (parentIdSiSco != null) {
          throw new GroupProgramCannotHaveParentException();
        }
        yield null;
      }
      case PROGRAM_OPTION -> {
        if (parentIdSiSco == null) {
          throw new GroupProgramOptionRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSiSco);
        if (parent.getType() != EGroupType.PROGRAM) {
          throw new GroupProgramOptionParentMustBeProgramException();
        }
        yield parent;
      }
      case STUDENT_GROUP -> {
        if (parentIdSiSco == null) {
          throw new GroupStudentGroupRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSiSco);
        if (parent.getType() != EGroupType.PROGRAM
            && parent.getType() != EGroupType.PROGRAM_OPTION) {
          throw new GroupStudentGroupParentMustBeProgramOrOptionException();
        }
        yield parent;
      }
    };
  }

  private Group findParentOrThrow(String parentIdSiSco) {
    return groupRepository.findByIdSiSco(parentIdSiSco).orElseThrow(GroupNotFoundException::new);
  }
}
