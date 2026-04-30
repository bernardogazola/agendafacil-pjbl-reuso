package br.pucpr.agendafacil.domain.notification;

/**
 * Tipo de notificação gerada pelo sistema.
 *
 * <p>Esse valor indica o contexto da mensagem, como confirmação de agendamento,
 * lembrete, cancelamento ou promoção. Ele pode ser usado na montagem do conteúdo
 * enviado ao cliente, junto com o canal escolhido para a notificação.</p>
 */
public enum NotificationType {
    APPOINTMENT_CONFIRMATION,
    REMINDER,
    CANCELLATION,
    PROMOTIONAL
}
