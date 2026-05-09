package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.business.ReviewApplicationService;
import br.pucpr.agendafacil.application.dto.CreateReviewRequest;
import br.pucpr.agendafacil.application.dto.ReviewResponse;
import br.pucpr.agendafacil.application.dto.UpdateReviewRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * Resource responsável pelos endpoints de avaliações.
 *
 * <p>Permite que clientes criem, consultem, atualizem e removam suas próprias
 * avaliações. Também permite que o dono consulte avaliações dos próprios
 * estabelecimentos.</p>
 */
@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Avaliações",
        description = "Avaliações deixadas por clientes em agendamentos concluídos"
)
public class ReviewResource {

    private final ReviewApplicationService reviewService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public ReviewResource(ReviewApplicationService reviewService, AuthenticatedUser authenticatedUser) {
        this.reviewService = reviewService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Cria uma avaliação para um agendamento concluído do cliente autenticado.
     *
     * @param appointmentId identificador do agendamento avaliado
     * @param req dados da avaliação
     * @return resposta HTTP 201 com a avaliação criada
     */
    @POST
    @Path("/appointments/{appointmentId}/review")
    @RolesAllowed("customer")
    @Operation(
            summary = "Cria uma avaliação",
            description = "Cria uma avaliação para um agendamento concluído pertencente ao cliente autenticado."
    )
    @APIResponse(
            responseCode = "201",
            description = "Avaliação criada",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
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
            description = "Usuário não tem permissão para avaliar este agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Agendamento ou cliente não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Avaliação já cadastrada para este agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Agendamento ainda não está concluído",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response create(
            @Parameter(description = "Identificador do agendamento", example = "42", required = true)
            @PathParam("appointmentId") Long appointmentId,

            @RequestBody(
                    description = "Dados da avaliação que será criada",
                    content = @Content(schema = @Schema(implementation = CreateReviewRequest.class))
            )
            @Valid CreateReviewRequest req) {
        ReviewResponse body = reviewService.createReview(
                authenticatedUser.id(), appointmentId, req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Lista as avaliações feitas pelo cliente autenticado.
     *
     * @return lista de avaliações do cliente
     */
    @GET
    @Path("/customers/me/reviews")
    @RolesAllowed("customer")
    @Operation(
            summary = "Lista minhas avaliações",
            description = "Retorna as avaliações feitas pelo cliente autenticado."
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
            description = "Usuário não tem permissão para acessar este recurso",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<ReviewResponse> listMine() {
        return reviewService.listMine(authenticatedUser.id());
    }

    /**
     * Lista as avaliações de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de avaliações relacionadas ao estabelecimento
     */
    @GET
    @Path("/businesses/{businessId}/reviews")
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

    /**
     * Busca uma avaliação feita pelo cliente autenticado.
     *
     * @param reviewId identificador da avaliação
     * @return dados da avaliação encontrada
     */
    @GET
    @Path("/reviews/{reviewId}")
    @RolesAllowed("customer")
    @Operation(
            summary = "Consulta uma avaliação",
            description = "Retorna os dados de uma avaliação feita pelo cliente autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Avaliação encontrada",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar esta avaliação",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Avaliação não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public ReviewResponse get(
            @Parameter(description = "Identificador da avaliação", example = "8", required = true)
            @PathParam("reviewId") Long reviewId) {
        return reviewService.getMine(authenticatedUser.id(), reviewId);
    }

    /**
     * Atualiza uma avaliação feita pelo cliente autenticado.
     *
     * @param reviewId identificador da avaliação
     * @param req novos dados da avaliação
     * @return avaliação atualizada
     */
    @PUT
    @Path("/reviews/{reviewId}")
    @RolesAllowed("customer")
    @Operation(
            summary = "Atualiza uma avaliação",
            description = "Atualiza uma avaliação feita pelo cliente autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Avaliação atualizada",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class))
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
            description = "Usuário não tem permissão para alterar esta avaliação",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Avaliação não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public ReviewResponse update(
            @Parameter(description = "Identificador da avaliação", example = "8", required = true)
            @PathParam("reviewId") Long reviewId,

            @RequestBody(
                    description = "Dados da avaliação que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateReviewRequest.class))
            )
            @Valid UpdateReviewRequest req) {
        return reviewService.updateMine(authenticatedUser.id(), reviewId, req);
    }

    /**
     * Remove uma avaliação feita pelo cliente autenticado.
     *
     * @param reviewId identificador da avaliação
     * @return resposta HTTP 204 sem corpo
     */
    @DELETE
    @Path("/reviews/{reviewId}")
    @RolesAllowed("customer")
    @Operation(
            summary = "Remove uma avaliação",
            description = "Remove uma avaliação feita pelo cliente autenticado."
    )
    @APIResponse(
            responseCode = "204",
            description = "Avaliação removida"
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para remover esta avaliação",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Avaliação não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response delete(
            @Parameter(description = "Identificador da avaliação", example = "8", required = true)
            @PathParam("reviewId") Long reviewId) {
        reviewService.deleteMine(authenticatedUser.id(), reviewId);
        return Response.noContent().build();
    }
}