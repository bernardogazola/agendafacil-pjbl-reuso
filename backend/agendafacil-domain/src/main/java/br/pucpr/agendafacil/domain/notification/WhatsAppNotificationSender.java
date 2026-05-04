package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Sender responsável pelo envio de notificações por WhatsApp.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link NotificationSender}. Ela reutiliza o fluxo geral de envio, mas adapta
 * a validação, a mensagem e o despacho para o canal WhatsApp.</p>
 *
 * <p>Assim como no SMS, o cliente precisa ter telefone cadastrado. A diferença
 * é que a mensagem pode ser mais informal e detalhada.</p>
 */
public class WhatsAppNotificationSender extends NotificationSender {

    @Override
    protected boolean validateRecipient(Appointment appointment) {
        String phone = appointment.getCustomer() == null ? null
                : appointment.getCustomer().getPhone();
        return phone != null && !phone.isBlank();
    }

    @Override
    protected String formatMessage(Appointment appointment) {
        return String.format(
                "Oi, %s! Lembrando que seu %s está marcado para %s. Até lá!",
                appointment.getCustomer().getName(),
                appointment.getOfferedService().getName(),
                appointment.getScheduledAt());
    }

    @Override
    protected void dispatch(String message, Appointment appointment) {
        System.out.println("[WHATSAPP -> " + appointment.getCustomer().getPhone()
                + "] " + message);
    }
}
