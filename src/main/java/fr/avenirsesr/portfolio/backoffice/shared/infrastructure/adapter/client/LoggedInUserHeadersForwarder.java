package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class LoggedInUserHeadersForwarder {

  private static final String DEV_EPPN_HEADER = "eppn";

  private final HttpServletRequest currentRequest;

  public LoggedInUserHeadersForwarder(HttpServletRequest currentRequest) {
    this.currentRequest = currentRequest;
  }

  public void forward(HttpHeaders headers) {
    copyHeader(headers, AvenirsSecurityHeaders.SIGNED_CONTEXT);
    copyHeader(headers, AvenirsSecurityHeaders.CONTEXT_SIGNATURE);
    copyHeader(headers, DEV_EPPN_HEADER);
  }

  private void copyHeader(HttpHeaders headers, String name) {
    String value = currentRequest.getHeader(name);
    if (value != null) {
      headers.set(name, value);
    }
  }
}
