package br.pucpr.agendafacil.adapter.in.web.identity;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.application.dto.CustomerResponse;
import br.pucpr.agendafacil.application.dto.UpdateCustomerRequest;
import br.pucpr.agendafacil.application.identity.CustomerApplicationService;
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
 * Resource responsável pela gestão administrativa de clientes.
 *
 * <p>Permite listar, consultar, atualizar, desativar e reativar clientes
 * cadastrados na plataforma. Todos os endpoints exigem autenticação com papel
 * {@code admin}.</p>
 */
@Path("/api/v1/admin/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(
        name = "Clientes Admin",
        description = "Gestão administrativa de clientes cadastrados"
)
public class AdminCustomerResource {

    private final CustomerApplicationService customerService;

    @Inject
    public AdminCustomerResource(CustomerApplicationService customerService) {
        this.customerService = customerService;
    }

    /**
     * Lista os clientes cadastrados.
     *
     * @param activeOnly indica se a listagem deve retornar apenas clientes ativos
     * @return lista de clientes encontrados
     */
    @GET
    @Operation(
            summary = "Lista clientes cadastrados",
            description = "Retorna os clientes cadastrados na plataforma. Quando activeOnly=true, retorna apenas clientes ativos."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de clientes",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = CustomerResponse.class
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
    public List<CustomerResponse> list(
            @Parameter(
                    description = "Quando true, retorna apenas clientes ativos",
                    example = "false",
                    schema = @Schema(defaultValue = "false")
            )
            @QueryParam("activeOnly")
            @DefaultValue("false")
            boolean activeOnly) {
        return customerService.list(activeOnly);
    }

    /**
     * Busca um cliente pelo identificador.
     *
     * @param id identificador do cliente
     * @return dados do cliente encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Consulta um cliente",
            description = "Retorna os dados de um cliente cadastrado a partir do seu identificador."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))
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
    public CustomerResponse get(
            @Parameter(description = "Identificador do cliente", example = "7", required = true)
            @PathParam("id") Long id) {
        return customerService.getById(id);
    }

    /**
     * Atualiza os dados cadastrais de um cliente.
     *
     * @param id identificador do cliente
     * @param request novos dados do cliente
     * @return cliente atualizado
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualiza um cliente",
            description = "Atualiza os dados cadastrais e as preferências de notificação de um cliente."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cliente atualizado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))
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
    public CustomerResponse update(
            @Parameter(description = "Identificador do cliente", example = "7", required = true)
            @PathParam("id") Long id,

            @RequestBody(
                    description = "Dados do cliente que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateCustomerRequest.class))
            )
            @Valid UpdateCustomerRequest request) {
        return customerService.update(id, request);
    }

    /**
     * Desativa o cadastro de um cliente.
     *
     * <p>O cliente não é removido do banco, apenas marcado como inativo para
     * preservar o histórico relacionado.</p>
     *
     * @param id identificador do cliente
     * @return cliente desativado
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Desativa um cliente",
            description = "Marca o cliente como inativo sem remover seu histórico da plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cliente desativado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))
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
    public CustomerResponse deactivate(
            @Parameter(description = "Identificador do cliente", example = "7", required = true)
            @PathParam("id") Long id) {
        return customerService.deactivate(id);
    }

    /**
     * Reativa o cadastro de um cliente.
     *
     * @param id identificador do cliente
     * @return cliente reativado
     */
    @POST
    @Path("/{id}/reactivate")
    @Operation(
            summary = "Reativa um cliente",
            description = "Marca novamente o cliente como ativo na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cliente reativado",
            content = @Content(schema = @Schema(implementation = CustomerResponse.class))
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
    public CustomerResponse reactivate(
            @Parameter(description = "Identificador do cliente", example = "7", required = true)
            @PathParam("id") Long id) {
        return customerService.reactivate(id);
    }
}