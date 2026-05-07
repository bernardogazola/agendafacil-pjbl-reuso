package br.pucpr.agendafacil.adapter.in.web.scheduling;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.application.dto.AvailableSlotsResponse;
import br.pucpr.agendafacil.application.scheduling.AvailabilityApplicationService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;

/**
 * Resource público responsável pela consulta de disponibilidade de serviços.
 *
 * <p>Permite consultar os horários livres de um serviço em uma data específica,
 * considerando os horários de funcionamento e os agendamentos já existentes.</p>
 */
@Path("/api/v1/businesses/{businessId}/services/{serviceId}")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Disponibilidade", description = "Consulta pública de horários livres para agendamento")
public class AvailabilityResource {

    private final AvailabilityApplicationService availabilityService;

    @Inject
    public AvailabilityResource(AvailabilityApplicationService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Lista os horários disponíveis para um serviço em uma data.
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @param date data desejada para consulta
     * @return horários disponíveis para agendamento
     */
    @GET
    @Path("/available-slots")
    @PermitAll
    @Operation(
            summary = "Lista horários disponíveis",
            description = "Retorna os horários livres de um serviço em uma data específica."
    )
    @APIResponse(
            responseCode = "200",
            description = "Horários disponíveis calculados com sucesso",
            content = @Content(schema = @Schema(implementation = AvailableSlotsResponse.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Parâmetros inválidos na requisição",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento ou serviço não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public AvailableSlotsResponse availableSlots(
            @Parameter(description = "Identificador do estabelecimento", example = "10", required = true)
            @PathParam("businessId") Long businessId,

            @Parameter(description = "Identificador do serviço", example = "5", required = true)
            @PathParam("serviceId") Long serviceId,

            @Parameter(
                    description = "Data desejada para consulta de disponibilidade",
                    example = "2026-05-20",
                    required = true,
                    schema = @Schema(format = "date")
            )
            @NotNull(message = "A data é obrigatória")
            @QueryParam("date") LocalDate date) {
        return availabilityService.getAvailableSlots(businessId, serviceId, date);
    }
}