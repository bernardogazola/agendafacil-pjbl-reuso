package br.pucpr.agendafacil.adapter.in.web.scheduling;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.AppointmentResponse;
import br.pucpr.agendafacil.application.scheduling.AppointmentApplicationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Path("/api/v1/businesses/{businessId}/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Agenda do estabelecimento", description = "Consulta de agendamentos pelo dono do estabelecimento")
public class BusinessAppointmentsResource {

    private final AppointmentApplicationService appointmentService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public BusinessAppointmentsResource(AppointmentApplicationService appointmentService, AuthenticatedUser authenticatedUser) {
        this.appointmentService = appointmentService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Lista os agendamentos de um estabelecimento em um intervalo de datas.
     *
     * <p>Quando o intervalo não é informado, usa uma janela padrão de 30 dias
     * antes até 30 dias depois da data atual.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param from data inicial da consulta
     * @param to data final da consulta
     * @return lista de agendamentos encontrados
     */
    @GET
    @RolesAllowed("owner")
    @Operation(
            summary = "Lista agendamentos do estabelecimento",
            description = "Retorna os agendamentos de um estabelecimento administrado pelo usuário autenticado dentro de uma janela de datas."
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
            responseCode = "400",
            description = "Parâmetros inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Usuário não tem permissão para consultar este estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public List<AppointmentResponse> list(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(
                    description = "Data inicial da consulta. Se omitida, usa 30 dias antes da data atual.",
                    example = "2026-05-01",
                    schema = @Schema(format = "date")
            )
            @QueryParam("from") LocalDate from,

            @Parameter(
                    description = "Data final da consulta. Se omitida, usa 30 dias após a data atual.",
                    example = "2026-05-31",
                    schema = @Schema(format = "date")
            )
            @QueryParam("to") LocalDate to) {
        LocalDate start = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate end = to == null ? LocalDate.now().plusDays(30) : to;
        return appointmentService.listForBusiness(authenticatedUser.id(), businessId, start, end);
    }
}