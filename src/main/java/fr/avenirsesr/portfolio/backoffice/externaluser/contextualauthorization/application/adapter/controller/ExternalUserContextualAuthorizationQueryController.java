package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.dto.ContextualAuthorizationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.mapper.ContextualAuthorizationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.input.ExternalUserContextualAuthorizationService;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/external-users/{externalUserId}/contextual-authorizations")
public class ExternalUserContextualAuthorizationQueryController {

  private final ExternalUserContextualAuthorizationService contextualAuthorizationService;

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping("/types")
  public ResponseEntity<List<EAuthorizationEntityType>> getAvailableTypes(
      @PathVariable UUID externalUserId) {
    log.debug("Getting the contextual authorization types of external user: {}", externalUserId);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(contextualAuthorizationService.getAvailableTypes(externalUserId));
  }

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping
  public ResponseEntity<PagedResponse<ContextualAuthorizationDTO>> getAuthorizations(
      @PathVariable UUID externalUserId,
      @Parameter(schema = @Schema(ref = "#/components/schemas/EAuthorizationEntityType"))
          @RequestParam
          EAuthorizationEntityType entityType,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Getting the {} contextual authorizations of external user: {} {}",
        entityType,
        externalUserId,
        pageCriteria);

    PagedResult<ContextualAuthorization> authorizations =
        contextualAuthorizationService.getAuthorizations(externalUserId, entityType, pageCriteria);

    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(
            new PagedResponse<>(
                authorizations.content().stream()
                    .map(ContextualAuthorizationApplicationMapper::toContextualAuthorizationDTO)
                    .toList(),
                PageInfoDTO.fromDomain(authorizations.pageInfo())));
  }
}
