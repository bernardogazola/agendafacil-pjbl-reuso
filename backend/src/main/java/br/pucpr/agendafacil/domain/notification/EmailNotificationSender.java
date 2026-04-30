package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Sender responsável pelo envio de notificações por e-mail.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link NotificationSender}. Ela reutiliza o fluxo geral de envio, mas adapta
 * a validação, a montagem da mensagem e o despacho para o canal de e-mail.</p>
 *
 * <p>O e-mail é usado como um canal mais formal, adequado para mensagens com
 * conteúdo um pouco mais completo, como confirmações de agendamento.</p>
 */
public class EmailNotificationSender extends NotificationSender {

    @Override
    protected boolean validateRecipient(Appointment appointment) {
        String email = appointment.getCustomer() == null ? null
                : appointment.getCustomer().getEmail();
        return email != null && email.contains("@");
    }

    @Override
    protected String formatMessage(Appointment appointment) {
        return String.format(
                "Olá %s, seu agendamento de %s foi confirmado para %s.",
                appointment.getCustomer().getName(),
                appointment.getOfferedService().getName(),
                appointment.getScheduledAt());
    }

    @Override
    protected void dispatch(String message, Appointment appointment) {
        System.out.println("[EMAIL -> " + appointment.getCustomer().getEmail()
                + "] " + message);
    }
}
