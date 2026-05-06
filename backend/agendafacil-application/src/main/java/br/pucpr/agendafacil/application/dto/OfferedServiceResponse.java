package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados de um serviço oferecido por um estabelecimento")
public record OfferedServiceResponse(
        @Schema(description = "Identificador do serviço", examples = "5", readOnly = true)
        Long id,

        @Schema(description = "Identificador do estabelecimento", examples = "10")
        Long businessId,

        @Schema(description = "Nome do serviço", examples = "Corte masculino")
        String name,

        @Schema(description = "Preço base do serviço", examples = "59.90")
        BigDecimal basePrice,

        @Schema(description = "Duração estimada do serviço em minutos", examples = "45")
        Integer durationMinutes,

        @Schema(description = "Descrição do serviço",
                nullable = true,
                examples = "Corte com tesoura e máquina")
        String description,

        @Schema(description = "Política de precificação aplicada ao serviço", examples = "FIXED")
        PricingPolicyType pricingPolicyType,

        @Schema(description = "Indica se o serviço está ativo", examples = "true")
        boolean active
) {}