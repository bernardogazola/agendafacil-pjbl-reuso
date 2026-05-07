package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;

/**
 * Requisição para atualização dos dados de um cliente.
 *
 * <p>Usada pelo painel administrativo para alterar informações cadastrais e
 * preferências de notificação do cliente.</p>
 */
@Schema(description = "Dados editáveis de um cliente")
public record UpdateCustomerRequest(
        @Schema(description = "Nome completo do cliente", examples = "Maria Cliente",
                minLength = 1, maxLength = 120, required = true)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String name,

        @Schema(description = "Telefone do cliente", nullable = true,
                examples = "+5541999999999", maxLength = 20)
        @Size(max = 20)
        String phone,

        @Schema(description = "Data de nascimento do cliente", format = "date",
                nullable = true, examples = "1998-04-12")
        LocalDate birthDate,

        @Schema(description = "Canais de notificação habilitados pelo cliente",
                nullable = true,
                uniqueItems = true,
                examples = "[\"EMAIL\", \"WHATSAPP\"]")
        Set<NotificationChannel> notificationPreferences
) {}