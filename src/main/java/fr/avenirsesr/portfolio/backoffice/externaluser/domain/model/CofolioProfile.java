package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import java.util.UUID;

/**
 * What the portfolio api knows of an external user: whether an account was ever created for its
 * eppn, which only happens on a first login to Cofolio, and the profile picture it uploaded since.
 */
public record CofolioProfile(String eppn, boolean hasConnected, UUID profilePictureId) {}
