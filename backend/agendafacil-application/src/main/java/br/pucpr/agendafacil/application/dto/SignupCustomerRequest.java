package br.pucpr.agendafacil.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Requisição para cadastro de um cliente.
 *
 * <p>Contém os dados básicos necessários para criar a conta do cliente final na
 * plataforma.</p>
 */
@Schema(description = "Dados para cadastro de cliente")
public record SignupCustomerRequest(
        @Schema(description = "Nome completo do cliente",
                examples = "João Silva",
                minLength = 1, maxLength = 120,
                required = true)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        @Schema(description = "E-mail do cliente",
                format = "email",
                examples = "cliente@email.com",
                maxLength = 160,
                required = true)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 160)
        String email,

        @Schema(description = "Senha do cliente",
                format = "password",
                writeOnly = true,
                examples = "senhaSegura@123",
                minLength = 8, maxLength = 120,
                required = true)
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 120, message = "A senha deve ter entre 8 e 120 caracteres")
        String password,

        @Schema(description = "Data de nascimento do cliente",
                format = "date",
                nullable = true,
                examples = "1998-04-12")
        LocalDate birthDate,

        @Schema(description = "Telefone do cliente",
                nullable = true,
                examples = "+5541999999999",
                maxLength = 20)
        @Size(max = 20)
        String phone
) {}