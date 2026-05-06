package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Credenciais para autenticação")
public record LoginRequest(
        @Schema(description = "E-mail cadastrado no sistema",
                format = "email",
                examples = "cliente@email.com",
                required = true)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @Schema(description = "Senha do usuário",
                format = "password",
                writeOnly = true,
                examples = "senhaSegura@123",
                required = true)
        @NotBlank(message = "A senha é obrigatória")
        String password
) {}