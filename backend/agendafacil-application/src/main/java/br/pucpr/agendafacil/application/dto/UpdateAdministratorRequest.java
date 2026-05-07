package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para atualização dos dados de um administrador.
 *
 * <p>A senha é opcional. Quando não for informada, a senha atual é mantida.</p>
 */
@Schema(description = "Dados editáveis de um administrador")
public record UpdateAdministratorRequest(
        @Schema(description = "Nome completo do administrador", examples = "Carlos Admin",
                minLength = 1, maxLength = 120, required = true)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        @Schema(description = "Telefone do administrador", nullable = true,
                examples = "+5541999999999", maxLength = 20)
        @Size(max = 20)
        String phone,

        @Schema(description = "Nível de acesso do administrador",
                examples = "BUSINESS_ADMIN", required = true)
        @NotNull(message = "O nível de acesso é obrigatório")
        AccessLevel accessLevel,

        @Schema(description = "Nova senha do administrador. Quando não informada, mantém a senha atual.",
                format = "password", writeOnly = true, nullable = true,
                examples = "novaSenhaSegura123", minLength = 8, maxLength = 120)
        @Size(min = 8, max = 120, message = "A senha deve ter entre 8 e 120 caracteres")
        String newPassword
) {}