package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para atualização de uma avaliação.
 */
@Schema(description = "Dados editáveis de uma avaliação")
public record UpdateReviewRequest(
        @Schema(description = "Nota da avaliação, de 1 a 5",
                examples = "4",
                minimum = "1",
                maximum = "5",
                required = true)
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota mínima é 1")
        @Max(value = 5, message = "A nota máxima é 5")
        Integer rating,

        @Schema(description = "Comentário da avaliação",
                nullable = true,
                examples = "Bom atendimento.",
                maxLength = 1000)
        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comment
) {}