package br.pucpr.agendafacil.adapter.in.web.notification;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.NotificationPreferencesRequest;
import br.pucpr.agendafacil.application.dto.NotificationPreferencesResponse;
import br.pucpr.agendafacil.application.notification.NotificationApplicationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Resource responsável pelas preferências de notificação do cliente autenticado.
 *
 * <p>Os endpoints sempre usam o cliente identificado pelo token da requisição,
 * sem permitir alteração das preferências de outros usuários por id.</p>
 */
@Path("/api/v1/customers/me/notification-preferences")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("customer")
@Tag(
        name = "Preferências de notificação",
        description = "Consulta e atualização dos canais de notificação do cliente autenticado"
)
public class NotificationPreferencesResource {

    private final NotificationApplicationService notificationService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public NotificationPreferencesResource(NotificationApplicationService notificationService,
                                           AuthenticatedUser authenticatedUser) {
        this.notificationService = notificationService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Retorna as preferências de notificação do cliente autenticado.
     *
     * @return canais de notificação atualmente habilitados
     */
    @GET
    @Operation(
            summary = "Consulta as preferências de notificação",
            description = "Retorna os canais de notificação configurados para o cliente autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Preferências encontradas",
            content = @Content(schema = @Schema(implementation = NotificationPreferencesResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar este recurso",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public NotificationPreferencesResponse getMyPreferences() {
        return notificationService.getMyPreferences(authenticatedUser.id());
    }

    /**
     * Atualiza as preferências de notificação do cliente autenticado.
     *
     * <p>O conjunto enviado substitui as preferências atuais. Para desativar
     * todos os canais, envie uma lista vazia.</p>
     *
     * @param request novos canais de notificação
     * @return preferências atualizadas
     */
    @PUT
    @Operation(
            summary = "Atualiza as preferências de notificação",
            description = "Substitui os canais de notificação configurados para o cliente autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Preferências atualizadas",
            content = @Content(schema = @Schema(implementation = NotificationPreferencesResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar este recurso",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public NotificationPreferencesResponse updateMyPreferences(
            @RequestBody(
                    description = "Novos canais de notificação do cliente",
                    content = @Content(schema = @Schema(implementation = NotificationPreferencesRequest.class))
            )
            @Valid NotificationPreferencesRequest request) {
        return notificationService.updateMyPreferences(authenticatedUser.id(), request);
    }
}