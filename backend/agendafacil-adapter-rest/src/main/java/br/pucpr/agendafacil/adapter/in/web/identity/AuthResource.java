package br.pucpr.agendafacil.adapter.in.web.identity;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.application.dto.LoginRequest;
import br.pucpr.agendafacil.application.dto.LoginResponse;
import br.pucpr.agendafacil.application.dto.SignupCustomerRequest;
import br.pucpr.agendafacil.application.dto.SignupOwnerRequest;
import br.pucpr.agendafacil.application.identity.AuthApplicationService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Resource responsável pelos endpoints de autenticação.
 *
 * <p>Permite cadastrar clientes, cadastrar donos de estabelecimento e autenticar
 * usuários já existentes na plataforma.</p>
 */
@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Cadastro de usuários e emissão de tokens de autenticação")
public class AuthResource {

    private final AuthApplicationService authService;

    @Inject
    public AuthResource(AuthApplicationService authService) {
        this.authService = authService;
    }

    /**
     * Cadastra um novo cliente e retorna seus dados de autenticação.
     *
     * @param req dados de cadastro do cliente
     * @return resposta HTTP 201 com token e dados básicos do cliente criado
     */
    @POST
    @Path("/customer/signup")
    @PermitAll
    @Operation(
            summary = "Cadastra um novo cliente",
            description = "Cria a conta do cliente final e retorna um token de autenticação com o papel de cliente."
    )
    @APIResponse(
            responseCode = "201",
            description = "Cliente criado e autenticado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response signupCustomer(
            @RequestBody(
                    description = "Dados do cliente que será cadastrado",
                    content = @Content(schema = @Schema(implementation = SignupCustomerRequest.class))
            )
            @Valid SignupCustomerRequest req) {
        LoginResponse body = authService.signupCustomer(req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Cadastra um dono de estabelecimento e seu primeiro estabelecimento.
     *
     * @param req dados do administrador e do estabelecimento
     * @return resposta HTTP 201 com token, dados do administrador e id do estabelecimento
     */
    @POST
    @Path("/owner/signup")
    @PermitAll
    @Operation(
            summary = "Cadastra um dono de estabelecimento",
            description = "Cria o administrador responsável e seu primeiro estabelecimento em um único fluxo de cadastro."
    )
    @APIResponse(
            responseCode = "201",
            description = "Dono e estabelecimento criados com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response signupOwner(
            @RequestBody(
                    description = "Dados do dono e do estabelecimento que serão cadastrados",
                    content = @Content(schema = @Schema(implementation = SignupOwnerRequest.class))
            )
            @Valid SignupOwnerRequest req) {
        LoginResponse body = authService.signupOwnerWithBusiness(req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Autentica um usuário cadastrado.
     *
     * @param req credenciais de acesso
     * @return token e dados básicos do usuário autenticado
     */
    @POST
    @Path("/login")
    @PermitAll
    @Operation(
            summary = "Autentica um usuário",
            description = "Valida as credenciais de acesso e retorna um token de autenticação."
    )
    @APIResponse(
            responseCode = "200",
            description = "Autenticação realizada com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public LoginResponse login(
            @RequestBody(
                    description = "Credenciais do usuário",
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid LoginRequest req) {
        return authService.login(req);
    }
}