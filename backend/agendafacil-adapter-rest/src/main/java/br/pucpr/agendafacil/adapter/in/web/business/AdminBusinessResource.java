package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.application.business.BusinessApplicationService;
import br.pucpr.agendafacil.application.dto.BusinessResponse;
import br.pucpr.agendafacil.application.dto.UpdateBusinessRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
 * Resource responsável pela gestão administrativa de estabelecimentos.
 *
 * <p>Permite listar, consultar, atualizar, desativar e reativar
 * estabelecimentos cadastrados na plataforma. Todos os endpoints exigem
 * autenticação com papel {@code admin}.</p>
 */
@Path("/api/v1/admin/businesses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(
        name = "Estabelecimentos Admin",
        description = "Gestão administrativa de estabelecimentos da plataforma"
)
public class AdminBusinessResource {

    private final BusinessApplicationService businessService;

    @Inject
    public AdminBusinessResource(BusinessApplicationService businessService) {
        this.businessService = businessService;
    }

    /**
     * Lista os estabelecimentos cadastrados na plataforma.
     *
     * @param activeOnly indica se a listagem deve retornar apenas estabelecimentos ativos
     * @return lista de estabelecimentos encontrados
     */
    @GET
    @Operation(
            summary = "Lista estabelecimentos cadastrados",
            description = "Retorna os estabelecimentos cadastrados na plataforma. Quando activeOnly=true, retorna apenas estabelecimentos ativos."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de estabelecimentos",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = BusinessResponse.class
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
    public List<BusinessResponse> list(
            @Parameter(
                    description = "Quando true, retorna apenas estabelecimentos ativos",
                    example = "false",
                    schema = @Schema(defaultValue = "false")
            )
            @QueryParam("activeOnly")
            @DefaultValue("false")
            boolean activeOnly) {
        return businessService.listAdminAll(activeOnly);
    }

    /**
     * Busca um estabelecimento pelo identificador.
     *
     * @param id identificador do estabelecimento
     * @return dados do estabelecimento encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Consulta um estabelecimento",
            description = "Retorna os dados de um estabelecimento cadastrado a partir do seu identificador."
    )
    @APIResponse(
            responseCode = "200",
            description = "Estabelecimento encontrado",
            content = @Content(schema = @Schema(implementation = BusinessResponse.class))
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
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse get(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long id) {
        return businessService.getBusinessAdmin(id);
    }

    /**
     * Atualiza os dados de um estabelecimento.
     *
     * @param id identificador do estabelecimento
     * @param request novos dados do estabelecimento
     * @return estabelecimento atualizado
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualiza um estabelecimento",
            description = "Atualiza os dados cadastrais, categoria, plano e política de cancelamento de um estabelecimento."
    )
    @APIResponse(
            responseCode = "200",
            description = "Estabelecimento atualizado",
            content = @Content(schema = @Schema(implementation = BusinessResponse.class))
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
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse update(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long id,

            @RequestBody(
                    description = "Dados do estabelecimento que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateBusinessRequest.class))
            )
            @Valid UpdateBusinessRequest request) {
        return businessService.updateBusinessAdmin(id, request);
    }

    /**
     * Desativa um estabelecimento.
     *
     * <p>O estabelecimento não é removido do banco, apenas marcado como inativo para
     * preservar o histórico relacionado.</p>
     *
     * @param id identificador do estabelecimento
     * @return estabelecimento desativado
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Desativa um estabelecimento",
            description = "Marca o estabelecimento como inativo, mantendo seu histórico na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Estabelecimento desativado",
            content = @Content(schema = @Schema(implementation = BusinessResponse.class))
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
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse deactivate(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long id) {
        return businessService.deactivateBusinessAdmin(id);
    }

    /**
     * Reativa um estabelecimento.
     *
     * @param id identificador do estabelecimento
     * @return estabelecimento reativado
     */
    @POST
    @Path("/{id}/reactivate")
    @Operation(
            summary = "Reativa um estabelecimento",
            description = "Marca novamente o estabelecimento como ativo na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Estabelecimento reativado",
            content = @Content(schema = @Schema(implementation = BusinessResponse.class))
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
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse reactivate(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long id) {
        return businessService.reactivateBusinessAdmin(id);
    }
}