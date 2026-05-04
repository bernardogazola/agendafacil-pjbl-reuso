package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Define o fluxo base para envio de notificações.
 *
 * <p>Esta classe aplica o padrão Template Method: o método {@link #send}
 * fixa a ordem das etapas do envio, enquanto as subclasses definem os detalhes
 * específicos de cada canal.</p>
 *
 * <p>O fluxo padrão valida o destinatário, formata a mensagem, registra a
 * tentativa de envio, despacha a notificação e trata possíveis falhas. Com isso,
 * e-mail, SMS e WhatsApp reutilizam a mesma estrutura, mudando apenas os passos
 * que realmente dependem do canal.</p>
 */
public abstract class NotificationSender {

    /**
     * Executa o fluxo completo de envio da notificação.
     *
     * <p>Este é o template method da classe. Por isso, é {@code final}: as
     * subclasses podem alterar os passos específicos, mas não a ordem geral do
     * envio.</p>
     */
    public final SendResult send(Appointment appointment) {
        if (!validateRecipient(appointment)) {
            return SendResult.fail("Destinatário inválido");
        }
        String message = formatMessage(appointment);
        logAttempt(appointment);
        try {
            dispatch(message, appointment);
            return SendResult.ok(message);
        } catch (Exception e) {
            SendResult failure = handleFailure(e);
            return SendResult.failAfterFormat(message, failure.errorMessage());
        }
    }

    /**
     * Valida se o destinatário possui os dados necessários para o canal usado.
     */
    protected abstract boolean validateRecipient(Appointment appointment);

    /**
     * Monta a mensagem que será enviada ao destinatário.
     */
    protected abstract String formatMessage(Appointment appointment);

    /**
     * Realiza o envio da mensagem pelo canal correspondente.
     */
    protected abstract void dispatch(String message, Appointment appointment) throws Exception;

    /**
     * Registra uma tentativa de envio.
     *
     * <p>A implementação padrão apenas escreve no console. Subclasses podem
     * sobrescrever este método caso precisem de outro comportamento.</p>
     */
    protected void logAttempt(Appointment appointment) {
        System.out.println("[notification] attempt channel="
                + getClass().getSimpleName()
                + " appointment=" + appointment.getId());
    }

    /**
     * Trata uma falha ocorrida durante o envio.
     *
     * <p>A implementação padrão transforma a exceção em um resultado de falha.
     * Subclasses podem sobrescrever este método para tratar erros específicos
     * do canal.</p>
     */
    protected SendResult handleFailure(Exception e) {
        return SendResult.fail(e.getMessage());
    }
}