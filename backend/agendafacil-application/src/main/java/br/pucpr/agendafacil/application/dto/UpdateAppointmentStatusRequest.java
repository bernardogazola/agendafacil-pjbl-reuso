package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para alteração de status de um agendamento pelo dono do
 * estabelecimento.
 */
@Schema(description = "Dados para alterar o status de um agendamento")
public record UpdateAppointmentStatusRequest(
        @Schema(description = "Ação que será aplicada ao agendamento",
                examples = "CONFIRM",
                required = true)
        @NotNull(message = "A ação é obrigatória")
        AppointmentStatusAction action
) {}