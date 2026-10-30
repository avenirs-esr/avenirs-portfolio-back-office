package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import java.util.UUID;

/**
 * What the portfolio api knows of an external user: whether an account was ever created for its
 * eppn, which only happens on a first login to Cofolio, the id of that account (null until then)
 * the profile picture it uploaded since, and whether it owns a staff and a student record: a
 * connected account may lack them, in which case its id cannot be used as a staff id.
 */
public record CofolioProfile(
    String eppn,
    boolean hasConnected,
    UUID userId,
    UUID profilePictureId,
    boolean staff,
    boolean student) {}
