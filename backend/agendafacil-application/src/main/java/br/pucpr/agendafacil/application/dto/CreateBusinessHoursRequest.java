package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Requisição para criação de uma janela de funcionamento.
 */
@Schema(description = "Dados para criação de uma janela de funcionamento")
public record CreateBusinessHoursRequest(
        @Schema(description = "Dia da semana",
                examples = "MONDAY",
                required = true)
        @NotNull(message = "O dia da semana é obrigatório")
        DayOfWeek dayOfWeek,

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

        @Schema(description = "Indica se a janela deve iniciar ativa",
                nullable = true,
                defaultValue = "true",
                examples = "true")
        Boolean active
) {}