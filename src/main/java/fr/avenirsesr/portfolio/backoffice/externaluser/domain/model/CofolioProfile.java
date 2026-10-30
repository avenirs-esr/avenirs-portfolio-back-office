package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EUniverse;
import java.util.Set;
import java.util.UUID;

/**
 * What the portfolio api knows of an external user: whether an account was ever created for its
 * eppn, which only happens on a first login to Cofolio, the id of that account (null until then)
 * the profile picture it uploaded since, and the universes whose record it owns: possibly both,
 * possibly none, in which case its id cannot be used as a staff id.
 */
public record CofolioProfile(
    String eppn,
    boolean hasConnected,
    UUID userId,
    UUID profilePictureId,
    Set<EUniverse> universes) {}
