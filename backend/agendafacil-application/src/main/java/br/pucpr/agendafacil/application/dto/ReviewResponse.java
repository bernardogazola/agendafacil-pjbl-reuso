package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Resposta com os dados de uma avaliação de atendimento.
 */
@Schema(description = "Dados de uma avaliação deixada por um cliente")
public record ReviewResponse(
        @Schema(description = "Identificador da avaliação",
                examples = "8",
                readOnly = true)
        Long id,

        @Schema(description = "Identificador do agendamento avaliado",
                examples = "42",
                readOnly = true)
        Long appointmentId,

        @Schema(description = "Identificador do cliente que fez a avaliação",
                examples = "7",
                readOnly = true)
        Long customerId,

        @Schema(description = "Nome do cliente que fez a avaliação",
                examples = "Maria Cliente",
                readOnly = true)
        String customerName,

        @Schema(description = "Identificador do estabelecimento avaliado",
                examples = "5",
                readOnly = true)
        Long businessId,

        @Schema(description = "Nome do estabelecimento avaliado",
                examples = "Barbearia Central",
                readOnly = true)
        String businessName,

        @Schema(description = "Identificador do serviço avaliado",
                examples = "3",
                readOnly = true)
        Long serviceId,

        @Schema(description = "Nome do serviço avaliado",
                examples = "Corte masculino",
                readOnly = true)
        String serviceName,

        @Schema(description = "Nota da avaliação, de 1 a 5",
                examples = "5",
                minimum = "1",
                maximum = "5")
        Integer rating,

        @Schema(description = "Comentário da avaliação",
                nullable = true,
                examples = "Atendimento excelente.")
        String comment
) {}