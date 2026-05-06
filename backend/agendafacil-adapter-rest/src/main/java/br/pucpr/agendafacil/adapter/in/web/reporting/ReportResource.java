package br.pucpr.agendafacil.adapter.in.web.reporting;

import br.pucpr.agendafacil.adapter.in.web.error.ApiError;
import br.pucpr.agendafacil.adapter.in.web.security.AuthenticatedUser;
import br.pucpr.agendafacil.application.dto.ReportResponse;
import br.pucpr.agendafacil.application.reporting.ReportApplicationService;
import br.pucpr.agendafacil.domain.reporting.ReportPeriod;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
 * Resource responsável pela geração de relatórios dos estabelecimentos.
 *
 * <p>Os relatórios são acessados pelo dono autenticado e retornam informações
 * consolidadas de agendamentos, como quantidade de atendimentos e receita do
 * período.</p>
 */
@Path("/api/v1/businesses/{businessId}/reports")
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Relatórios",
        description = "Relatórios de desempenho dos estabelecimentos"
)
public class ReportResource {

    private final ReportApplicationService reportService;
    private final AuthenticatedUser authenticatedUser;

    @Inject
    public ReportResource(ReportApplicationService reportService, AuthenticatedUser authenticatedUser) {
        this.reportService = reportService;
        this.authenticatedUser = authenticatedUser;
    }

    /**
     * Gera um relatório de agendamentos para um estabelecimento.
     *
     * <p>Quando a data de referência não é informada, a data atual do servidor
     * é usada para calcular o período.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param period período desejado para o relatório
     * @param date data de referência usada no cálculo do período
     * @return relatório gerado para o estabelecimento
     */
    @GET
    @RolesAllowed("owner")
    @Operation(
            summary = "Gera um relatório de agendamentos",
            description = "Gera um relatório diário, semanal ou mensal a partir de uma data de referência."
    )
    @APIResponse(
            responseCode = "200",
            description = "Relatório gerado com sucesso",
            content = @Content(schema = @Schema(implementation = ReportResponse.class))
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
            description = "Usuário não tem permissão para consultar relatórios deste estabelecimento",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Estabelecimento não encontrado",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    public ReportResponse generate(
            @Parameter(
                    description = "Identificador do estabelecimento",
                    example = "10",
                    required = true
            )
            @PathParam("businessId") Long businessId,

            @Parameter(
                    description = "Período do relatório",
                    example = "MONTHLY",
                    schema = @Schema(defaultValue = "DAILY")
            )
            @QueryParam("period")
            @DefaultValue("DAILY")
            ReportPeriod period,

            @Parameter(
                    description = "Data de referência para calcular o período. Se omitida, usa a data atual.",
                    example = "2026-05-06",
                    schema = @Schema(format = "date")
            )
            @QueryParam("date")
            LocalDate date) {
        LocalDate ref = date == null ? LocalDate.now() : date;
        return reportService.generateReport(authenticatedUser.id(), businessId, period, ref);
    }
}