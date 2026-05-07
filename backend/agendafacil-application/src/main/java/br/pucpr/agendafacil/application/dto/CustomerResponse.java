package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;

/**
 * Resposta com os dados de um cliente cadastrado.
 */
@Schema(description = "Dados de um cliente cadastrado na plataforma")
public record CustomerResponse(
        @Schema(description = "Identificador do cliente", examples = "7", readOnly = true)
        Long id,

        @Schema(description = "Nome completo do cliente", examples = "Maria Cliente")
        String name,

        @Schema(description = "E-mail cadastrado do cliente", format = "email",
                examples = "cliente@email.com")
        String email,

        @Schema(description = "Telefone do cliente", nullable = true,
                examples = "+5541999999999")
        String phone,

        @Schema(description = "Data de nascimento do cliente", format = "date",
                nullable = true, examples = "1998-04-12")
        LocalDate birthDate,

        @Schema(description = "Canais de notificação habilitados pelo cliente",
                uniqueItems = true,
                examples = "[\"EMAIL\", \"WHATSAPP\"]")
        Set<NotificationChannel> notificationPreferences,

        @Schema(description = "Indica se o cadastro do cliente está ativo",
                examples = "true")
        boolean active
) {}