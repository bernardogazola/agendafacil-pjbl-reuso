package br.pucpr.agendafacil.domain.notification;

/**
 * Status de envio de uma notificação.
 *
 * <p>Uma notificação começa como {@link #PENDING} e depois pode ser marcada
 * como {@link #SENT} ou {@link #FAILED}, conforme o resultado da tentativa de
 * envio.</p>
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
