package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalTime;

/**
 * Requisição para atualização de uma janela de funcionamento.
 *
 * <p>A atualização altera horários e status de atividade, mas mantém o dia da
 * semana da janela original.</p>
 */
@Schema(description = "Dados editáveis de uma janela de funcionamento")
public record UpdateBusinessHoursEntryRequest(
        @Schema(description = "Horário de abertura",
                format = "time",
                examples = "09:00",
                required = true)
        @NotNull(message = "O horário de abertura é obrigatório")
        LocalTime startTime,

        @Schema(description = "Horário de fechamento",
                format = "time",
                examples = "18:00",
                required = true)
        @NotNull(message = "O horário de fechamento é obrigatório")
        LocalTime endTime,

        @Schema(description = "Indica se a janela está ativa para agendamentos",
                examples = "true",
                required = true)
        @NotNull(message = "Indique se o dia está ativo")
        Boolean active
) {}