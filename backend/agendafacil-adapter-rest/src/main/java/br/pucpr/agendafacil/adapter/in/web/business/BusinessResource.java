package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.business.BusinessApplicationService;
import br.pucpr.agendafacil.application.dto.BusinessHoursDTO;
import br.pucpr.agendafacil.application.dto.CreateOfferedServiceRequest;
import br.pucpr.agendafacil.application.dto.OfferedServiceResponse;
import br.pucpr.agendafacil.application.dto.UpdateBusinessHoursRequest;
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
 * Resource responsável pelas operações do dono sobre um estabelecimento.
 *
 * <p>Permite cadastrar serviços oferecidos e atualizar os horários de
 * funcionamento. Todos os endpoints exigem autenticação com papel
 * {@code owner}.</p>
 */
@Path("/api/v1/businesses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Estabelecimento", description = "Gestão de serviços e horários pelo dono do estabelecimento")
public class BusinessResource {

    private final BusinessApplicationService businessService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public BusinessResource(BusinessApplicationService businessService, AuthenticatedUser authenticatedUser) {
        this.businessService = businessService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Cadastra um novo serviço em um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param req dados do serviço que será criado
     * @return resposta HTTP 201 com os dados do serviço criado
     */
    @POST
    @Path("/{id}/services")
    @RolesAllowed("owner")
    @Operation(
            summary = "Cadastra um novo serviço no estabelecimento",
            description = "Cria um serviço oferecido por um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "201",
            description = "Serviço criado com sucesso",
            content = @Content(schema = @Schema(implementation = OfferedServiceResponse.class))
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
            description = "Usuário não tem permissão para alterar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response createService(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long businessId,

            @RequestBody(
                    description = "Dados do serviço que será cadastrado",
                    content = @Content(schema = @Schema(implementation = CreateOfferedServiceRequest.class))
            )
            @Valid CreateOfferedServiceRequest req) {
        OfferedServiceResponse body = businessService.createOfferedService(
                authenticatedUser.id(), businessId, req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Atualiza os horários de funcionamento de um estabelecimento.
     *
     * <p>Para cada dia informado, o horário existente é atualizado ou uma nova
     * janela é criada quando ainda não houver cadastro para aquele dia.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param req horários que serão aplicados
     * @return lista atualizada de horários de funcionamento
     */
    @PUT
    @Path("/{id}/hours")
    @RolesAllowed("owner")
    @Operation(
            summary = "Atualiza os horários de funcionamento do estabelecimento",
            description = "Atualiza ou cria as janelas de funcionamento informadas para um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Horários atualizados com sucesso",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = BusinessHoursDTO.class
            ))
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
            description = "Usuário não tem permissão para alterar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<BusinessHoursDTO> updateHours(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("id") Long businessId,

            @RequestBody(
                    description = "Horários de funcionamento que serão aplicados",
                    content = @Content(schema = @Schema(implementation = UpdateBusinessHoursRequest.class))
            )
            @Valid UpdateBusinessHoursRequest req) {
        return businessService.updateBusinessHours(authenticatedUser.id(), businessId, req);
    }
}