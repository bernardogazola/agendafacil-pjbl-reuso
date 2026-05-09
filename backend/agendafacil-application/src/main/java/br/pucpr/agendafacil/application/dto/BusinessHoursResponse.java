package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Resposta com os dados de uma janela de funcionamento.
 */
@Schema(description = "Dados de uma janela de funcionamento de um estabelecimento")
public record BusinessHoursResponse(
        @Schema(description = "Identificador da janela de funcionamento",
                examples = "15",
                readOnly = true)
        Long id,

        @Schema(description = "Dia da semana",
                examples = "MONDAY")
        DayOfWeek dayOfWeek,

        @Schema(description = "Horário de abertura",
                format = "time",
                examples = "09:00")
        LocalTime startTime,

        @Schema(description = "Horário de fechamento",
                format = "time",
                examples = "18:00")
        LocalTime endTime,

        @Schema(description = "Indica se a janela está disponível para agendamentos",
                examples = "true")
        boolean active
) {}