package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Requisição para criação de um administrador.
 *
 * <p>Usada pelo painel administrativo para cadastrar novos administradores da
 * plataforma ou donos de estabelecimento.</p>
 */
@Schema(description = "Dados para criação de um administrador")
public record CreateAdministratorRequest(
        @Schema(description = "Nome completo do administrador", examples = "Carlos Admin",
                minLength = 1, maxLength = 120, required = true)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        @Schema(description = "E-mail do administrador", format = "email",
                examples = "admin@email.com", maxLength = 160, required = true)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 160)
        String email,

        @Schema(description = "Senha do administrador", format = "password",
                writeOnly = true, examples = "senhaSegura123",
                minLength = 8, maxLength = 120, required = true)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 120, message = "A senha deve ter entre 8 e 120 caracteres")
        String password,

        @Schema(description = "Telefone do administrador", nullable = true,
                examples = "+5541999999999", maxLength = 20)
        @Size(max = 20)
        String phone,

        @Schema(description = "Nível de acesso do administrador",
                examples = "BUSINESS_ADMIN", required = true)
        @NotNull(message = "O nível de acesso é obrigatório")
        AccessLevel accessLevel
) {}