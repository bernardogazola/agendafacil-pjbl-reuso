package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Requisição para atualização de uma campanha promocional.
 *
 * <p>A promoção deve informar apenas uma forma de desconto: percentual ou valor
 * fixo. Quando a lista de serviços elegíveis estiver vazia, a promoção vale
 * para todos os serviços do estabelecimento.</p>
 */
@Schema(description = "Dados editáveis de uma campanha promocional")
public record UpdatePromotionRequest(
        @Schema(description = "Nome da promoção",
                examples = "Semana do Cliente",
                minLength = 1,
                maxLength = 150,
                required = true)
        @NotBlank(message = "O nome da promoção é obrigatório")
        @Size(max = 150)
        String name,

        @Schema(description = "Descrição da promoção",
                nullable = true,
                examples = "Desconto especial para serviços selecionados",
                maxLength = 500)
        @Size(max = 500)
        String description,

        @Schema(description = "Percentual de desconto. Não deve ser usado junto com discountAmount.",
                nullable = true,
                examples = "15.00",
                minimum = "0.01",
                maximum = "100.00")
        @DecimalMin(value = "0.01", message = "O desconto percentual deve ser positivo")
        @DecimalMax(value = "100.00", message = "O desconto percentual deve ser no máximo 100")
        BigDecimal discountPercentage,

        @Schema(description = "Valor fixo de desconto. Não deve ser usado junto com discountPercentage.",
                nullable = true,
                examples = "20.00",
                minimum = "0.01")
        @DecimalMin(value = "0.01", message = "O desconto deve ser positivo")
        BigDecimal discountAmount,

        @Schema(description = "Data inicial da promoção",
                format = "date",
                examples = "2026-05-01",
                required = true)
        @NotNull(message = "A data inicial da promoção é obrigatória")
        LocalDate validFrom,

        @Schema(description = "Data final da promoção",
                format = "date",
                examples = "2026-05-31",
                required = true)
        @NotNull(message = "A data final da promoção é obrigatória")
        LocalDate validTo,

        @Schema(description = "Serviços elegíveis para a promoção. Quando nulo ou vazio, a promoção vale para todos os serviços.",
                nullable = true,
                uniqueItems = true,
                examples = "[1, 2, 3]")
        List<Long> eligibleServiceIds,

        @Schema(description = "Indica se a promoção está ativa",
                examples = "true",
                required = true)
        @NotNull(message = "Indique se a promoção está ativa")
        Boolean active
) {}