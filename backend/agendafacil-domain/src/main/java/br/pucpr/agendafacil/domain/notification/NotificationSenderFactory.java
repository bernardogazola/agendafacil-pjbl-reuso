package br.pucpr.agendafacil.domain.notification;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory responsável por escolher o sender adequado para cada canal de
 * notificação.
 *
 * <p>Ela complementa o Template Method usado em {@link NotificationSender}: o
 * fluxo geral de envio fica na classe abstrata, enquanto esta factory apenas
 * decide qual implementação concreta deve ser usada para e-mail, SMS ou
 * WhatsApp.</p>
 *
 * <p>Com isso, o serviço de aplicação não precisa conhecer os detalhes de cada
 * canal. Caso um novo canal seja adicionado, basta criar um novo sender e
 * incluí-lo nesta resolução.</p>
 */
@ApplicationScoped
public class NotificationSenderFactory {

    /**
     * Retorna o sender correspondente ao canal informado.
     *
     * @param channel canal de envio da notificação
     * @return sender responsável pelo canal
     */
    public NotificationSender resolve(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> new EmailNotificationSender();
            case SMS -> new SmsNotificationSender();
            case WHATSAPP -> new WhatsAppNotificationSender();
        };
    }
}
