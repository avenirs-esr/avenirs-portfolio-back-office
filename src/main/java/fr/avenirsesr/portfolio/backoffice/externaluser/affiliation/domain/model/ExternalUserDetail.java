package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import java.util.List;

public record ExternalUserDetail(
    ExternalUser externalUser, Institution institution, List<Group> groups) {}
