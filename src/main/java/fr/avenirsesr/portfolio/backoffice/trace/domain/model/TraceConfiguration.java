package fr.avenirsesr.portfolio.backoffice.trace.domain.model;

public record TraceConfiguration(
    int maxRemainingDays, int maxRemainingDaysBeforeWarning, int maxRemainingDaysBeforeCritical) {}
