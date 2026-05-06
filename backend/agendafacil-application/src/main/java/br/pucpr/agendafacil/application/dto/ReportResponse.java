package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.reporting.ReportPeriod;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Relatório de desempenho de um estabelecimento em um período")
public record ReportResponse(
        @Schema(description = "Tipo de período usado no relatório", examples = "MONTHLY")
        ReportPeriod period,

        @Schema(description = "Data inicial do período", format = "date", examples = "2026-05-01")
        LocalDate startDate,

        @Schema(description = "Data final do período", format = "date", examples = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "Quantidade de atendimentos no período", examples = "128")
        int appointmentCount,

        @Schema(description = "Receita total do período", examples = "8420.50")
        BigDecimal totalRevenue,

        @Schema(description = "Resumo textual do relatório",
                examples = "Relatório de 2026-05-01 a 2026-05-31: 128 atendimentos, R$ 8420.50 de receita")
        String formatted
) {}