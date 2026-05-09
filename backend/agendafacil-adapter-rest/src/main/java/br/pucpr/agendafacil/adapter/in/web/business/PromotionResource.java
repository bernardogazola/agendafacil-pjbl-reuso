package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.business.PromotionApplicationService;
import br.pucpr.agendafacil.application.dto.CreatePromotionRequest;
import br.pucpr.agendafacil.application.dto.PromotionResponse;
import br.pucpr.agendafacil.application.dto.UpdatePromotionRequest;
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
 * Resource responsável pela gestão de promoções de um estabelecimento.
 *
 * <p>Permite que o dono autenticado liste, consulte, crie, atualize, desative e
 * reative campanhas promocionais do próprio estabelecimento.</p>
 */
@Path("/api/v1/businesses/{businessId}/promotions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("owner")
@Tag(
        name = "Promoções",
        description = "Gestão de campanhas promocionais pelo dono do estabelecimento"
)
public class PromotionResource {

    private final PromotionApplicationService promotionService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public PromotionResource(PromotionApplicationService promotionService, AuthenticatedUser authenticatedUser) {
        this.promotionService = promotionService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Lista as promoções de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de promoções do estabelecimento
     */
    @GET
    @Operation(
            summary = "Lista promoções do estabelecimento",
            description = "Retorna as campanhas promocionais cadastradas para um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de promoções",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = PromotionResponse.class
            ))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<PromotionResponse> list(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId) {
        return promotionService.listForBusiness(authenticatedUser.id(), businessId);
    }

    /**
     * Busca uma promoção de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return dados da promoção encontrada
     */
    @GET
    @Path("/{promotionId}")
    @Operation(
            summary = "Consulta uma promoção",
            description = "Retorna os dados de uma promoção pertencente a um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Promoção encontrada",
            content = @Content(schema = @Schema(implementation = PromotionResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar esta promoção",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Promoção não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public PromotionResponse get(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da promoção", example = "3", required = true)
            @PathParam("promotionId") Long promotionId) {
        return promotionService.getById(authenticatedUser.id(), businessId, promotionId);
    }

    /**
     * Cria uma nova promoção para um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param req dados da promoção que será criada
     * @return resposta HTTP 201 com a promoção criada
     */
    @POST
    @Operation(
            summary = "Cria uma nova promoção",
            description = "Cria uma campanha promocional para um estabelecimento. Informe exatamente um tipo de desconto: percentual ou valor fixo."
    )
    @APIResponse(
            responseCode = "201",
            description = "Promoção criada",
            content = @Content(schema = @Schema(implementation = PromotionResponse.class))
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
            description = "Usuário não tem permissão para alterar este estabelecimento ou algum serviço informado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento ou serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Regra de negócio rejeitou a promoção",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response create(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @RequestBody(
                    description = "Dados da promoção que será criada",
                    content = @Content(schema = @Schema(implementation = CreatePromotionRequest.class))
            )
            @Valid CreatePromotionRequest req) {
        PromotionResponse body = promotionService.create(authenticatedUser.id(), businessId, req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Atualiza uma promoção de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @param req novos dados da promoção
     * @return promoção atualizada
     */
    @PUT
    @Path("/{promotionId}")
    @Operation(
            summary = "Atualiza uma promoção",
            description = "Atualiza os dados de uma promoção pertencente a um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Promoção atualizada",
            content = @Content(schema = @Schema(implementation = PromotionResponse.class))
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
            description = "Usuário não tem permissão para alterar esta promoção ou algum serviço informado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Promoção, estabelecimento ou serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Regra de negócio rejeitou a promoção",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public PromotionResponse update(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da promoção", example = "3", required = true)
            @PathParam("promotionId") Long promotionId,

            @RequestBody(
                    description = "Dados da promoção que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdatePromotionRequest.class))
            )
            @Valid UpdatePromotionRequest req) {
        return promotionService.update(authenticatedUser.id(), businessId, promotionId, req);
    }

    /**
     * Desativa uma promoção de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return promoção desativada
     */
    @DELETE
    @Path("/{promotionId}")
    @Operation(
            summary = "Desativa uma promoção",
            description = "Marca a promoção como inativa, mantendo seu histórico na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Promoção desativada",
            content = @Content(schema = @Schema(implementation = PromotionResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para alterar esta promoção",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Promoção não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public PromotionResponse deactivate(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da promoção", example = "3", required = true)
            @PathParam("promotionId") Long promotionId) {
        return promotionService.deactivate(authenticatedUser.id(), businessId, promotionId);
    }

    /**
     * Reativa uma promoção de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return promoção reativada
     */
    @POST
    @Path("/{promotionId}/reactivate")
    @Operation(
            summary = "Reativa uma promoção",
            description = "Marca novamente a promoção como ativa."
    )
    @APIResponse(
            responseCode = "200",
            description = "Promoção reativada",
            content = @Content(schema = @Schema(implementation = PromotionResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para alterar esta promoção",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Promoção não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public PromotionResponse reactivate(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da promoção", example = "3", required = true)
            @PathParam("promotionId") Long promotionId) {
        return promotionService.reactivate(authenticatedUser.id(), businessId, promotionId);
    }
}