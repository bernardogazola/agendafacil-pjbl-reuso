package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para cadastro de um dono de estabelecimento.
 *
 * <p>Cria o administrador responsável e o primeiro estabelecimento em um único
 * fluxo de cadastro.</p>
 */
@Schema(description = "Dados para cadastro de dono e estabelecimento")
public record SignupOwnerRequest(
        @Schema(description = "Nome completo do administrador",
                examples = "Maria Oliveira",
                minLength = 1, maxLength = 120,
                required = true)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120)
        String ownerName,

        @Schema(description = "E-mail de login do administrador",
                format = "email",
                examples = "admin@barbeariacentral.com",
                maxLength = 160,
                required = true)
        @NotBlank(message = "O e-mail do administrador é obrigatório")
        @Email(message = "E-mail do administrador inválido")
        @Size(max = 160)
        String ownerEmail,

        @Schema(description = "Senha do administrador",
                format = "password",
                writeOnly = true,
                examples = "senhaSegura@123",
                minLength = 8, maxLength = 120,
                required = true)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 120, message = "A senha deve ter entre 8 e 120 caracteres")
        String ownerPassword,

        @Schema(description = "Nome fantasia do estabelecimento",
                examples = "Barbearia Central",
                minLength = 1, maxLength = 200,
                required = true)
        @NotBlank(message = "O nome do estabelecimento é obrigatório")
        @Size(max = 200)
        String businessTradeName,

        @Schema(description = "E-mail de contato do estabelecimento",
                format = "email",
                examples = "contato@barbeariacentral.com",
                maxLength = 160,
                required = true)
        @NotBlank(message = "O e-mail do estabelecimento é obrigatório")
        @Email(message = "E-mail do estabelecimento inválido")
        @Size(max = 160)
        String businessEmail,

        @Schema(description = "Categoria do estabelecimento", examples = "BARBER_SHOP", required = true)
        @NotNull(message = "A categoria é obrigatória")
        BusinessCategory category,

        @Schema(description = "Plano contratado pelo estabelecimento", examples = "PROFESSIONAL", required = true)
        @NotNull(message = "O plano é obrigatório")
        BusinessPlan plan
) {}