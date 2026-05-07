package br.pucpr.agendafacil.adapter.in.web.identity;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.AdministratorResponse;
import br.pucpr.agendafacil.application.dto.CreateAdministratorRequest;
import br.pucpr.agendafacil.application.dto.UpdateAdministratorRequest;
import br.pucpr.agendafacil.application.identity.AdministratorApplicationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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
 * Resource responsável pela gestão administrativa de administradores.
 *
 * <p>Permite listar, consultar, criar, atualizar, desativar e reativar
 * administradores da plataforma. Todos os endpoints exigem autenticação com
 * papel {@code admin}.</p>
 */
@Path("/api/v1/admin/administrators")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(
        name = "Administradores",
        description = "Gestão administrativa de administradores da plataforma"
)
public class AdminAdministratorResource {

    private final AdministratorApplicationService administratorService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public AdminAdministratorResource(AdministratorApplicationService administratorService, AuthenticatedUser authenticatedUser) {
        this.administratorService = administratorService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Lista os administradores cadastrados.
     *
     * @param activeOnly indica se a listagem deve retornar apenas administradores ativos
     * @return lista de administradores encontrados
     */
    @GET
    @Operation(
            summary = "Lista administradores cadastrados",
            description = "Retorna os administradores cadastrados na plataforma. Quando activeOnly=true, retorna apenas administradores ativos."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de administradores",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = AdministratorResponse.class
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
    public List<AdministratorResponse> list(
            @Parameter(
                    description = "Quando true, retorna apenas administradores ativos",
                    example = "false",
                    schema = @Schema(defaultValue = "false")
            )
            @QueryParam("activeOnly")
            @DefaultValue("false")
            boolean activeOnly) {
        return administratorService.list(activeOnly);
    }

    /**
     * Busca um administrador pelo identificador.
     *
     * @param id identificador do administrador
     * @return dados do administrador encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Consulta um administrador",
            description = "Retorna os dados de um administrador cadastrado a partir do seu identificador."
    )
    @APIResponse(
            responseCode = "200",
            description = "Administrador encontrado",
            content = @Content(schema = @Schema(implementation = AdministratorResponse.class))
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
            description = "Administrador não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AdministratorResponse get(
            @Parameter(description = "Identificador do administrador", example = "3", required = true)
            @PathParam("id") Long id) {
        return administratorService.getById(id);
    }

    /**
     * Cria um novo administrador.
     *
     * @param request dados do administrador que será criado
     * @return resposta HTTP 201 com os dados do administrador criado
     */
    @POST
    @Operation(
            summary = "Cria um administrador",
            description = "Cadastra um novo administrador da plataforma."
    )
    @APIResponse(
            responseCode = "201",
            description = "Administrador criado",
            content = @Content(schema = @Schema(implementation = AdministratorResponse.class))
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
            responseCode = "409",
            description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response create(
            @RequestBody(
                    description = "Dados do administrador que será cadastrado",
                    content = @Content(schema = @Schema(implementation = CreateAdministratorRequest.class))
            )
            @Valid CreateAdministratorRequest request) {
        AdministratorResponse body = administratorService.create(request);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Atualiza os dados de um administrador.
     *
     * @param id identificador do administrador
     * @param request novos dados do administrador
     * @return administrador atualizado
     */
    @PUT
    @Path("/{id}")
    @Operation(
            summary = "Atualiza um administrador",
            description = "Atualiza os dados cadastrais, o nível de acesso e, quando informada, a senha do administrador."
    )
    @APIResponse(
            responseCode = "200",
            description = "Administrador atualizado",
            content = @Content(schema = @Schema(implementation = AdministratorResponse.class))
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
            description = "Administrador não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AdministratorResponse update(
            @Parameter(description = "Identificador do administrador", example = "3", required = true)
            @PathParam("id") Long id,

            @RequestBody(
                    description = "Dados do administrador que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateAdministratorRequest.class))
            )
            @Valid UpdateAdministratorRequest request) {
        return administratorService.update(id, request);
    }

    /**
     * Desativa o cadastro de um administrador.
     *
     * <p>O administrador não é removido do banco, apenas marcado como inativo.
     * A operação não permite auto-desativação nem desativação do último super
     * administrador ativo.</p>
     *
     * @param id identificador do administrador
     * @return administrador desativado
     */
    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Desativa um administrador",
            description = "Marca o administrador como inativo, sem remover seu histórico da plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Administrador desativado",
            content = @Content(schema = @Schema(implementation = AdministratorResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Operação não permitida",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Administrador não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AdministratorResponse deactivate(
            @Parameter(description = "Identificador do administrador", example = "3", required = true)
            @PathParam("id") Long id) {
        return administratorService.deactivate(id, authenticatedUser.id());
    }

    /**
     * Reativa o cadastro de um administrador.
     *
     * @param id identificador do administrador
     * @return administrador reativado
     */
    @POST
    @Path("/{id}/reactivate")
    @Operation(
            summary = "Reativa um administrador",
            description = "Marca novamente o administrador como ativo na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Administrador reativado",
            content = @Content(schema = @Schema(implementation = AdministratorResponse.class))
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
            description = "Administrador não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AdministratorResponse reactivate(
            @Parameter(description = "Identificador do administrador", example = "3", required = true)
            @PathParam("id") Long id) {
        return administratorService.reactivate(id);
    }
}