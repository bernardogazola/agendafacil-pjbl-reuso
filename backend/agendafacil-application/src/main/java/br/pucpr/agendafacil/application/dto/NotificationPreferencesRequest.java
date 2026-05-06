package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Set;

/**
 * Requisição para atualizar as preferências de notificação do cliente.
 *
 * <p>O conjunto informado substitui completamente os canais anteriores. Quando
 * enviado vazio, significa que o cliente optou por não receber notificações em
 * nenhum canal.</p>
 */
@Schema(description = "Preferências de notificação do cliente")
public record NotificationPreferencesRequest(
        @Schema(description = "Canais de notificação habilitados pelo cliente. Envie vazio para desativar todos.",
                required = true, uniqueItems = true,
                examples = "[\"EMAIL\", \"WHATSAPP\"]")
        @NotNull(message = "O conjunto de canais é obrigatório (use um array vazio para optar por não receber).")
        Set<NotificationChannel> channels
) {}