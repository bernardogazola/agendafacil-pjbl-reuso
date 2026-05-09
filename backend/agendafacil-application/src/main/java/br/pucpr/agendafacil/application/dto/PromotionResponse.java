package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resposta com os dados de uma campanha promocional.
 */
@Schema(description = "Dados de uma campanha promocional cadastrada pelo estabelecimento")
public record PromotionResponse(
        @Schema(description = "Identificador da promoção",
                examples = "12",
                readOnly = true)
        Long id,

        @Schema(description = "Identificador do estabelecimento",
                examples = "5",
                readOnly = true)
        Long businessId,

        @Schema(description = "Nome da promoção",
                examples = "Semana do Cliente")
        String name,

        @Schema(description = "Descrição da promoção",
                nullable = true,
                examples = "Desconto especial para serviços selecionados")
        String description,

        @Schema(description = "Percentual de desconto aplicado pela promoção",
                nullable = true,
                examples = "15.00")
        BigDecimal discountPercentage,

        @Schema(description = "Valor fixo de desconto aplicado pela promoção",
                nullable = true,
                examples = "20.00")
        BigDecimal discountAmount,

        @Schema(description = "Data inicial da vigência da promoção",
                format = "date",
                examples = "2026-05-01")
        LocalDate validFrom,

        @Schema(description = "Data final da vigência da promoção",
                format = "date",
                examples = "2026-05-31")
        LocalDate validTo,

        @Schema(description = "Serviços elegíveis para a promoção. Lista vazia indica que a promoção vale para todos os serviços.",
                uniqueItems = true,
                examples = "[1, 2, 3]")
        List<Long> eligibleServiceIds,

        @Schema(description = "Indica se a promoção está ativa",
                examples = "true")
        boolean active
) {}