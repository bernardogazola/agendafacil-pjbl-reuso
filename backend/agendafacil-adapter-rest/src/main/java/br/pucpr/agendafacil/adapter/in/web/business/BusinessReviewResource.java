package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.business.ReviewApplicationService;
import br.pucpr.agendafacil.application.dto.ReviewResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Resource responsável pelos endpoints de avaliações vinculados a um
 * estabelecimento específico.
 */
@Path("/api/v1/businesses/{businessId}/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Avaliações",
        description = "Avaliações deixadas por clientes em agendamentos concluídos"
)
public class BusinessReviewResource {

    private final ReviewApplicationService reviewService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public BusinessReviewResource(
            ReviewApplicationService reviewService,
            AuthenticatedUser authenticatedUser) {
        this.reviewService = reviewService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Lista as avaliações de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de avaliações relacionadas ao estabelecimento
     */
    @GET
    @RolesAllowed("owner")
    @Operation(
            summary = "Lista avaliações do estabelecimento",
            description = "Retorna as avaliações relacionadas aos agendamentos de um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de avaliações",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = ReviewResponse.class
            ))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar avaliações deste estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<ReviewResponse> listForBusiness(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId) {
        return reviewService.listForBusiness(authenticatedUser.id(), businessId);
    }
}
