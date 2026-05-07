package br.pucpr.agendafacil.adapter.in.web.scheduling;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.AppointmentResponse;
import br.pucpr.agendafacil.application.dto.BookAppointmentRequest;
import br.pucpr.agendafacil.application.dto.CancellationResponse;
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
 * Resource responsável pelos agendamentos do cliente autenticado.
 *
 * <p>Permite criar agendamentos, cancelar uma reserva existente e consultar os
 * próprios agendamentos do cliente.</p>
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
     * @param id identificador do agendamento
     * @return resultado do cancelamento
     */
    @DELETE
    @Path("/{id}")
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
            @PathParam("id") Long id) {
        return appointmentService.cancelAppointment(id, authenticatedUser.id());
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
}