package br.pucpr.agendafacil.domain.notification.port;

import br.pucpr.agendafacil.domain.notification.Notification;

/**
 * Repositório responsável pelo acesso aos dados de {@link Notification}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: o serviço de
 * aplicação depende dela para registrar tentativas de envio de notificações,
 * sem conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface NotificationRepository {

    /**
     * Persiste uma notificação gerada pelo sistema.
     *
     * <p>A notificação pode representar uma tentativa enviada com sucesso,
     * uma falha de envio ou um registro ainda pendente.</p>
     *
     * @param notification notificação a ser salva
     */
    void persist(Notification notification);
}