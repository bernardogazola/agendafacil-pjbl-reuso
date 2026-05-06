package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados para criação de um agendamento")
public record BookAppointmentRequest(
        @Schema(description = "Identificador do estabelecimento", examples = "10", required = true)
        @NotNull(message = "O estabelecimento é obrigatório")
        Long businessId,

        @Schema(description = "Identificador do serviço ofertado", examples = "5", required = true)
        @NotNull(message = "O serviço é obrigatório")
        Long serviceId,

        @Schema(description = "Data e hora desejadas para o agendamento",
                format = "date-time",
                examples = "2026-05-20T14:30:00",
                required = true)
        @NotNull(message = "A data/hora do agendamento é obrigatória")
        LocalDateTime scheduledAt,

        @Schema(description = "Observações para o prestador",
                nullable = true,
                maxLength = 500,
                examples = "Tenho preferência por atendimento no início do horário")
        @Size(max = 500)
        String notes
) {}