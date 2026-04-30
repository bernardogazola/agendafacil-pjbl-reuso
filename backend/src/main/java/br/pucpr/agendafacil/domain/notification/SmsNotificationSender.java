package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Sender responsável pelo envio de notificações por SMS.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link NotificationSender}. Ela mantém o fluxo geral de envio, mas adapta os
 * passos específicos para o canal SMS.</p>
 *
 * <p>Neste canal, o destinatário precisa ter telefone cadastrado e a mensagem
 * é mais curta, adequada para lembretes rápidos de agendamento.</p>
 */
public class SmsNotificationSender extends NotificationSender {

    @Override
    protected boolean validateRecipient(Appointment appointment) {
        String phone = appointment.getCustomer() == null ? null
                : appointment.getCustomer().getPhone();
        return phone != null && !phone.isBlank();
    }

    @Override
    protected String formatMessage(Appointment appointment) {
        return String.format("AgendaFacil: %s em %s. Confirme sua presenca.",
                appointment.getOfferedService().getName(),
                appointment.getScheduledAt());
    }

    @Override
    protected void dispatch(String message, Appointment appointment) {
        System.out.println("[SMS -> " + appointment.getCustomer().getPhone()
                + "] " + message);
    }
}
