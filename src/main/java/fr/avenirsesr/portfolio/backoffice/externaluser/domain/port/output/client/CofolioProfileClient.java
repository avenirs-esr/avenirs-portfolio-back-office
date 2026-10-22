package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import java.util.Collection;
import java.util.Map;

public interface CofolioProfileClient {

  /**
   * Resolves the Cofolio profiles of the given eppns, keyed by eppn. An eppn the api reports
   * nothing for is absent from the map, and read by its callers as somebody who never logged in.
   */
  Map<String, CofolioProfile> findByEppns(Collection<String> eppns);
}
