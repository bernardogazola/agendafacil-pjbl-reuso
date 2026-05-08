package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para atualização dos dados de um estabelecimento.
 */
@Schema(description = "Dados editáveis de um estabelecimento")
public record UpdateBusinessRequest(
        @Schema(description = "Nome fantasia do estabelecimento",
                examples = "Barbearia Central",
                minLength = 1,
                maxLength = 200,
                required = true)
        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(max = 200)
        String tradeName,

        @Schema(description = "E-mail de contato do estabelecimento",
                format = "email",
                examples = "contato@barbeariacentral.com",
                maxLength = 160,
                required = true)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 160)
        String email,

        @Schema(description = "Telefone de contato do estabelecimento",
                nullable = true,
                examples = "+5541999999999",
                maxLength = 20)
        @Size(max = 20)
        String phone,

        @Schema(description = "Categoria do estabelecimento",
                examples = "BARBER_SHOP",
                required = true)
        @NotNull(message = "A categoria é obrigatória")
        BusinessCategory category,

        @Schema(description = "Plano contratado pelo estabelecimento",
                examples = "PROFESSIONAL",
                required = true)
        @NotNull(message = "O plano é obrigatório")
        BusinessPlan plan,

        @Schema(description = "Política de cancelamento aplicada aos agendamentos",
                examples = "FREE",
                required = true)
        @NotNull(message = "A política de cancelamento é obrigatória")
        CancellationPolicyType cancellationPolicyType
) {}