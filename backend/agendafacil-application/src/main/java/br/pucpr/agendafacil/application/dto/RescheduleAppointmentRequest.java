package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Requisição para reagendamento de um agendamento existente.
 */
@Schema(description = "Dados para reagendar um agendamento")
public record RescheduleAppointmentRequest(
        @Schema(description = "Nova data e hora do agendamento",
                format = "date-time",
                examples = "2026-05-20T14:30:00",
                required = true)
        @NotNull(message = "A nova data/hora do agendamento é obrigatória")
        @Future(message = "O novo horário deve estar no futuro")
        LocalDateTime newScheduledAt
) {}
