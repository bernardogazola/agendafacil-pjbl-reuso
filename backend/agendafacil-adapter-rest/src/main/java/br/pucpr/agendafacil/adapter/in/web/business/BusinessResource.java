package br.pucpr.agendafacil.adapter.in.web.business;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.business.BusinessApplicationService;
import br.pucpr.agendafacil.application.dto.*;
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
 * Resource responsável pelas operações do dono sobre seus estabelecimentos.
 *
 * <p>Permite consultar e atualizar o estabelecimento, gerenciar serviços
 * oferecidos e editar os horários de funcionamento. Todos os endpoints exigem
 * autenticação com papel {@code owner}.</p>
 */
@Path("/api/v1/businesses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Estabelecimento",
        description = "Gestão de estabelecimentos, serviços e horários pelo dono"
)
public class BusinessResource {

    private final BusinessApplicationService businessService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public BusinessResource(BusinessApplicationService businessService, AuthenticatedUser authenticatedUser) {
        this.businessService = businessService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Busca os dados de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @return dados do estabelecimento encontrado
     */
    @GET
    @Path("/{businessId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Consulta meu estabelecimento",
            description = "Retorna os dados de um estabelecimento administrado pelo usuário autenticado."
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
            description = "Usuário não tem permissão para acessar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse getBusiness(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId) {
        return businessService.getBusiness(authenticatedUser.id(), businessId);
    }

    /**
     * Atualiza os dados de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param req novos dados do estabelecimento
     * @return estabelecimento atualizado
     */
    @PUT
    @Path("/{businessId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Atualiza meu estabelecimento",
            description = "Atualiza os dados de um estabelecimento administrado pelo usuário autenticado."
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
            description = "Usuário não tem permissão para alterar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessResponse updateBusiness(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @RequestBody(
                    description = "Novos dados do estabelecimento",
                    content = @Content(schema = @Schema(implementation = UpdateBusinessRequest.class))
            )
            @Valid UpdateBusinessRequest req) {
        return businessService.updateBusiness(authenticatedUser.id(), businessId, req);
    }

    /**
     * Cadastra um novo serviço em um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param req dados do serviço que será criado
     * @return resposta HTTP 201 com os dados do serviço criado
     */
    @POST
    @Path("/{businessId}/services")
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
            @PathParam("businessId") Long businessId,

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
     * Busca um serviço do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return dados do serviço encontrado
     */
    @GET
    @Path("/{businessId}/services/{serviceId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Consulta um serviço",
            description = "Retorna os dados de um serviço pertencente a um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Serviço encontrado",
            content = @Content(schema = @Schema(implementation = OfferedServiceResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar este serviço",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public OfferedServiceResponse getService(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador do serviço", example = "5", required = true)
            @PathParam("serviceId") Long serviceId) {
        return businessService.getOfferedService(authenticatedUser.id(), businessId, serviceId);
    }

    /**
     * Atualiza um serviço do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @param req novos dados do serviço
     * @return serviço atualizado
     */
    @PUT
    @Path("/{businessId}/services/{serviceId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Atualiza um serviço",
            description = "Atualiza os dados de um serviço pertencente a um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Serviço atualizado",
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
            description = "Usuário não tem permissão para alterar este serviço",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public OfferedServiceResponse updateService(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador do serviço", example = "5", required = true)
            @PathParam("serviceId") Long serviceId,

            @RequestBody(
                    description = "Dados do serviço que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateOfferedServiceRequest.class))
            )
            @Valid UpdateOfferedServiceRequest req) {
        return businessService.updateOfferedService(
                authenticatedUser.id(), businessId, serviceId, req);
    }

    /**
     * Desativa um serviço do estabelecimento do dono autenticado.
     *
     * <p>O serviço não é removido do banco, apenas marcado como inativo para
     * preservar o histórico relacionado.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return serviço desativado
     */
    @DELETE
    @Path("/{businessId}/services/{serviceId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Desativa um serviço",
            description = "Marca o serviço como inativo, mantendo seu histórico na plataforma."
    )
    @APIResponse(
            responseCode = "200",
            description = "Serviço desativado",
            content = @Content(schema = @Schema(implementation = OfferedServiceResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para alterar este serviço",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public OfferedServiceResponse deactivateService(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador do serviço", example = "5", required = true)
            @PathParam("serviceId") Long serviceId) {
        return businessService.deactivateOfferedService(
                authenticatedUser.id(), businessId, serviceId);
    }

    /**
     * Reativa um serviço do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return serviço reativado
     */
    @POST
    @Path("/{businessId}/services/{serviceId}/reactivate")
    @RolesAllowed("owner")
    @Operation(
            summary = "Reativa um serviço",
            description = "Marca novamente o serviço como ativo e disponível para uso."
    )
    @APIResponse(
            responseCode = "200",
            description = "Serviço reativado",
            content = @Content(schema = @Schema(implementation = OfferedServiceResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para alterar este serviço",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public OfferedServiceResponse reactivateService(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador do serviço", example = "5", required = true)
            @PathParam("serviceId") Long serviceId) {
        return businessService.reactivateOfferedService(
                authenticatedUser.id(), businessId, serviceId);
    }

    /**
     * Lista as janelas de funcionamento de um estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de janelas de funcionamento
     */
    @GET
    @Path("/{businessId}/hours")
    @RolesAllowed("owner")
    @Operation(
            summary = "Lista horários de funcionamento",
            description = "Retorna as janelas de funcionamento de um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de janelas de funcionamento",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = BusinessHoursResponse.class
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
    public List<BusinessHoursResponse> listHours(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId) {
        return businessService.listHours(authenticatedUser.id(), businessId);
    }

    /**
     * Busca uma janela de funcionamento do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param hourId identificador da janela de funcionamento
     * @return dados da janela de funcionamento
     */
    @GET
    @Path("/{businessId}/hours/{hourId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Consulta uma janela de funcionamento",
            description = "Retorna os dados de uma janela de funcionamento pertencente a um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Janela de funcionamento encontrada",
            content = @Content(schema = @Schema(implementation = BusinessHoursResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para acessar esta janela",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Janela de funcionamento não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessHoursResponse getHour(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da janela de funcionamento", example = "3", required = true)
            @PathParam("hourId") Long hourId) {
        return businessService.getHourById(authenticatedUser.id(), businessId, hourId);
    }

    /**
     * Cria uma nova janela de funcionamento para um estabelecimento.
     *
     * @param businessId identificador do estabelecimento
     * @param req dados da janela de funcionamento
     * @return resposta HTTP 201 com a janela criada
     */
    @POST
    @Path("/{businessId}/hours")
    @RolesAllowed("owner")
    @Operation(
            summary = "Cria uma janela de funcionamento",
            description = "Cria uma nova janela de funcionamento para um dia da semana do estabelecimento."
    )
    @APIResponse(
            responseCode = "201",
            description = "Janela de funcionamento criada",
            content = @Content(schema = @Schema(implementation = BusinessHoursResponse.class))
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
    @APIResponse(
            responseCode = "409",
            description = "Já existe janela para este dia ou o horário informado é inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response createHour(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @RequestBody(
                    description = "Dados da janela de funcionamento que será criada",
                    content = @Content(schema = @Schema(implementation = CreateBusinessHoursRequest.class))
            )
            @Valid CreateBusinessHoursRequest req) {
        BusinessHoursResponse body = businessService.createHour(
                authenticatedUser.id(), businessId, req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Atualiza uma janela de funcionamento do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param hourId identificador da janela de funcionamento
     * @param req novos dados da janela de funcionamento
     * @return janela de funcionamento atualizada
     */
    @PUT
    @Path("/{businessId}/hours/{hourId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Atualiza uma janela de funcionamento",
            description = "Atualiza os horários e o status de atividade de uma janela de funcionamento."
    )
    @APIResponse(
            responseCode = "200",
            description = "Janela de funcionamento atualizada",
            content = @Content(schema = @Schema(implementation = BusinessHoursResponse.class))
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
            description = "Usuário não tem permissão para alterar esta janela",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Janela de funcionamento não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Horário de abertura deve ser anterior ao de fechamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public BusinessHoursResponse updateHour(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da janela de funcionamento", example = "3", required = true)
            @PathParam("hourId") Long hourId,

            @RequestBody(
                    description = "Dados da janela de funcionamento que serão atualizados",
                    content = @Content(schema = @Schema(implementation = UpdateBusinessHoursEntryRequest.class))
            )
            @Valid UpdateBusinessHoursEntryRequest req) {
        return businessService.updateHour(authenticatedUser.id(), businessId, hourId, req);
    }

    /**
     * Remove uma janela de funcionamento do estabelecimento do dono autenticado.
     *
     * @param businessId identificador do estabelecimento
     * @param hourId identificador da janela de funcionamento
     * @return resposta HTTP 204 sem corpo
     */
    @DELETE
    @Path("/{businessId}/hours/{hourId}")
    @RolesAllowed("owner")
    @Operation(
            summary = "Remove uma janela de funcionamento",
            description = "Remove uma janela de funcionamento de um estabelecimento administrado pelo usuário autenticado."
    )
    @APIResponse(
            responseCode = "204",
            description = "Janela de funcionamento removida"
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para remover esta janela",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Janela de funcionamento não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response deleteHour(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador da janela de funcionamento", example = "3", required = true)
            @PathParam("hourId") Long hourId) {
        businessService.deleteHour(authenticatedUser.id(), businessId, hourId);
        return Response.noContent().build();
    }

    /**
     * Atualiza em lote os horários de funcionamento de um estabelecimento.
     *
     * <p>Para cada dia informado, o horário existente é atualizado ou uma nova
     * janela é criada quando ainda não houver cadastro para aquele dia.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param req horários que serão aplicados
     * @return lista atualizada de horários de funcionamento
     */
    @PUT
    @Path("/{businessId}/hours")
    @RolesAllowed("owner")
    @Operation(
            summary = "Atualiza horários de funcionamento em lote",
            description = "Atualiza ou cria várias janelas de funcionamento enviadas para um estabelecimento administrado pelo usuário autenticado."
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
            @PathParam("businessId") Long businessId,

            @RequestBody(
                    description = "Horários de funcionamento que serão aplicados",
                    content = @Content(schema = @Schema(implementation = UpdateBusinessHoursRequest.class))
            )
            @Valid UpdateBusinessHoursRequest req) {
        return businessService.updateBusinessHours(authenticatedUser.id(), businessId, req);
    }
}