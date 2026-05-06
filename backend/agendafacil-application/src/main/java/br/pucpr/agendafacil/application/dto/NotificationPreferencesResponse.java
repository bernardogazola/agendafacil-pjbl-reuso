package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Set;

/**
 * Resposta com as preferências de notificação do cliente.
 *
 * <p>Quando o conjunto estiver vazio, significa que nenhum canal de notificação
 * está habilitado.</p>
 */
@Schema(description = "Preferências de notificação do cliente")
public record NotificationPreferencesResponse(
        @Schema(description = "Canais de notificação atualmente habilitados",
                uniqueItems = true,
                examples = "[\"EMAIL\", \"WHATSAPP\"]")
        Set<NotificationChannel> channels
) {}