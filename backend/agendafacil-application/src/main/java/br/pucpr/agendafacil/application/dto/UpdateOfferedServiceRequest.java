package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Requisição para atualização dos dados de um serviço oferecido.
 */
@Schema(description = "Dados editáveis de um serviço oferecido")
public record UpdateOfferedServiceRequest(
        @Schema(description = "Nome do serviço",
                examples = "Corte masculino",
                minLength = 1,
                maxLength = 150,
                required = true)
        @NotBlank(message = "O nome do serviço é obrigatório")
        @Size(max = 150)
        String name,

        @Schema(description = "Preço base do serviço",
                examples = "59.90",
                minimum = "0.01",
                required = true)
        @NotNull(message = "O preço base é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço base deve ser positivo")
        BigDecimal basePrice,

        @Schema(description = "Duração estimada do serviço em minutos",
                examples = "45",
                minimum = "1",
                required = true)
        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser positiva")
        Integer durationMinutes,

        @Schema(description = "Descrição do serviço",
                nullable = true,
                examples = "Corte com tesoura e máquina",
                maxLength = 500)
        @Size(max = 500)
        String description,

        @Schema(description = "Política de precificação aplicada ao serviço",
                examples = "FIXED",
                required = true)
        @NotNull(message = "A política de preço é obrigatória")
        PricingPolicyType pricingPolicyType
) {}