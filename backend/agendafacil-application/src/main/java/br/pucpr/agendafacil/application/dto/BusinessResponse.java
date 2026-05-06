package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dados de um estabelecimento cadastrado")
public record BusinessResponse(
        @Schema(description = "Identificador do estabelecimento", examples = "10", readOnly = true)
        Long id,

        @Schema(description = "Nome fantasia do estabelecimento", examples = "Barbearia Central")
        String tradeName,

        @Schema(description = "E-mail de contato do estabelecimento",
                format = "email",
                examples = "contato@barbeariacentral.com")
        String email,

        @Schema(description = "Telefone de contato do estabelecimento",
                nullable = true,
                examples = "+5541999999999")
        String phone,

        @Schema(description = "Categoria do estabelecimento", examples = "BARBER_SHOP")
        BusinessCategory category,

        @Schema(description = "Plano contratado pelo estabelecimento", examples = "PROFESSIONAL")
        BusinessPlan plan,

        @Schema(description = "Política de cancelamento configurada", examples = "FREE")
        CancellationPolicyType cancellationPolicyType,

        @Schema(description = "Indica se o estabelecimento está ativo", examples = "true")
        boolean active
) {}