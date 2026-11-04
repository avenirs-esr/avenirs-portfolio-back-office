package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"eppn", "firstName", "lastName"})
public record StaffSearchItemResponse(String eppn, String firstName, String lastName) {}
