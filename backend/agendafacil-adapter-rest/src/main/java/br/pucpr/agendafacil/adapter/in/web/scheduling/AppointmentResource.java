package br.pucpr.agendafacil.adapter.in.web.scheduling;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.*;
import br.pucpr.agendafacil.application.scheduling.AppointmentApplicationService;
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
 * Resource responsável pelos endpoints de agendamento.
 *
 * <p>Permite que clientes criem, cancelem e consultem seus agendamentos. Também
 * expõe operações do dono do estabelecimento, como alteração de status e
 * reagendamento.</p>
 */
@Path("/api/v1/appointments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Agendamentos", description = "Criação, cancelamento e consulta de agendamentos do cliente")
public class AppointmentResource {

    private final AppointmentApplicationService appointmentService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public AppointmentResource(AppointmentApplicationService appointmentService, AuthenticatedUser authenticatedUser) {
        this.appointmentService = appointmentService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Cria um novo agendamento para o cliente autenticado.
     *
     * @param req dados do agendamento solicitado
     * @return resposta HTTP 201 com os dados do agendamento criado
     */
    @POST
    @RolesAllowed("customer")
    @Operation(
            summary = "Cria um novo agendamento",
            description = "Cria um agendamento para o cliente autenticado em um estabelecimento ativo."
    )
    @APIResponse(
            responseCode = "201",
            description = "Agendamento criado com sucesso",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
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
            description = "Usuário não tem permissão para criar agendamentos",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento ou serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Horário indisponível para agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Regra de negócio rejeitou o agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public Response book(
            @RequestBody(
                    description = "Dados do agendamento que será criado",
                    content = @Content(schema = @Schema(implementation = BookAppointmentRequest.class))
            )
            @Valid BookAppointmentRequest req) {
        AppointmentResponse body = appointmentService.bookAppointment(authenticatedUser.id(), req);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    /**
     * Cancela um agendamento do cliente autenticado.
     *
     * @param appointmentId identificador do agendamento
     * @return resultado do cancelamento
     */
    @DELETE
    @Path("/{appointmentId}")
    @RolesAllowed("customer")
    @Operation(
            summary = "Cancela um agendamento",
            description = "Solicita o cancelamento de um agendamento pertencente ao cliente autenticado."
    )
    @APIResponse(
            responseCode = "200",
            description = "Cancelamento processado",
            content = @Content(schema = @Schema(implementation = CancellationResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para cancelar este agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Agendamento não encontrado para este cliente",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "O status atual do agendamento não permite cancelamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Política de cancelamento não permitiu a operação",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public CancellationResponse cancel(
            @Parameter(description = "Identificador do agendamento", example = "42", required = true)
            @PathParam("appointmentId") Long appointmentId) {
        return appointmentService.cancelAppointment(appointmentId, authenticatedUser.id());
    }

    /**
     * Lista os agendamentos do cliente autenticado.
     *
     * @param includeAll indica se a resposta deve incluir também agendamentos não ativos
     * @return lista de agendamentos do cliente
     */
    @GET
    @Path("/me")
    @RolesAllowed("customer")
    @Operation(
            summary = "Lista meus agendamentos",
            description = "Retorna os agendamentos do cliente autenticado. Por padrão, lista apenas agendamentos ativos."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de agendamentos",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = AppointmentResponse.class
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
    public List<AppointmentResponse> mine(
            @Parameter(
                    description = "Quando true, inclui também agendamentos cancelados, concluídos ou históricos",
                    example = "false",
                    schema = @Schema(defaultValue = "false")
            )
            @QueryParam("includeAll")
            @DefaultValue("false")
            boolean includeAll) {
        return appointmentService.listForCustomer(authenticatedUser.id(), includeAll);
    }

    /**
     * Altera o status de um agendamento pela visão do dono do estabelecimento.
     *
     * @param appointmentId identificador do agendamento
     * @param req ação de mudança de status
     * @return agendamento atualizado
     */
    @PATCH
    @Path("/{appointmentId}/status")
    @RolesAllowed("owner")
    @Operation(
            summary = "Altera o status de um agendamento",
            description = "Permite que o dono do estabelecimento confirme, conclua ou marque um agendamento como não comparecido."
    )
    @APIResponse(
            responseCode = "200",
            description = "Status atualizado",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
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
            description = "Usuário não tem permissão para alterar este agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Agendamento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Transição de status não permitida",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AppointmentResponse changeStatus(
            @Parameter(description = "Identificador do agendamento", example = "42", required = true)
            @PathParam("appointmentId") Long appointmentId,

            @RequestBody(
                    description = "Ação de mudança de status que será aplicada",
                    content = @Content(schema = @Schema(implementation = UpdateAppointmentStatusRequest.class))
            )
            @Valid UpdateAppointmentStatusRequest req) {
        return appointmentService.changeStatus(appointmentId, authenticatedUser.id(), req);
    }

    /**
     * Reagenda um agendamento ativo pela visão do dono do estabelecimento.
     *
     * @param appointmentId identificador do agendamento
     * @param req nova data e hora do agendamento
     * @return agendamento reagendado
     */
    @PATCH
    @Path("/{appointmentId}/schedule")
    @RolesAllowed("owner")
    @Operation(
            summary = "Reagenda um agendamento",
            description = "Atualiza a data e hora de um agendamento ativo, verificando se o novo horário está disponível."
    )
    @APIResponse(
            responseCode = "200",
            description = "Agendamento reagendado",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
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
            description = "Usuário não tem permissão para reagendar este agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Agendamento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Novo horário indisponível para agendamento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "422",
            description = "Agendamento não está em estado ativo",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AppointmentResponse reschedule(
            @Parameter(description = "Identificador do agendamento", example = "42", required = true)
            @PathParam("appointmentId") Long appointmentId,

            @RequestBody(
                    description = "Nova data e hora do agendamento",
                    content = @Content(schema = @Schema(implementation = RescheduleAppointmentRequest.class))
            )
            @Valid RescheduleAppointmentRequest req) {
        return appointmentService.reschedule(appointmentId, authenticatedUser.id(), req);
    }
}