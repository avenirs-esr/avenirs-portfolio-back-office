package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums;

/**
 * Role declared by the institutions on the external user import payload. The domain itself keeps
 * working with {@link fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory}: the
 * service maps {@code TEACHER} and {@code SUIO} onto {@code STAFF}, and {@code STUDENT} onto {@code
 * STUDENT}.
 */
public enum EExternalUserRole {
  TEACHER,
  SUIO,
  STUDENT
}
