package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resultado de uma solicitação de cancelamento")
public record CancellationResponse(
        @Schema(description = "Indica se o cancelamento foi efetivado", examples = "true")
        boolean success,

        @Schema(description = "Status final do agendamento após a solicitação", examples = "CANCELED")
        AppointmentStatus status,

        @Schema(description = "Valor da multa aplicada no cancelamento", examples = "0.00")
        BigDecimal fee,

        @Schema(description = "Mensagem explicando o resultado do cancelamento",
                nullable = true,
                examples = "Cancelamento realizado com sucesso")
        String reason
) {}