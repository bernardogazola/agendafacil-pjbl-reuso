package br.pucpr.agendafacil.application.notification;

import br.pucpr.agendafacil.application.dto.NotificationPreferencesRequest;
import br.pucpr.agendafacil.application.dto.NotificationPreferencesResponse;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import br.pucpr.agendafacil.domain.notification.*;
import br.pucpr.agendafacil.domain.notification.port.NotificationRepository;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.shared.configuration.SystemConfiguration;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Serviço de aplicação responsável pelo envio de notificações e pelas
 * preferências de canais do cliente.
 *
 * <p>O envio usa o {@link NotificationSender} adequado para cada canal
 * habilitado pelo cliente. Cada tentativa é registrada como uma
 * {@link Notification}, mesmo quando ocorre falha no envio.</p>
 *
 * <p>As notificações são tratadas como melhor esforço: uma falha em um canal
 * não impede a tentativa nos demais canais configurados.</p>
 */
@ApplicationScoped
public class NotificationApplicationService {

    private static final Logger log = Logger.getLogger(NotificationApplicationService.class);

    private final NotificationSenderFactory senderFactory;
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    @Inject
    public NotificationApplicationService(NotificationSenderFactory senderFactory,
                                          NotificationRepository notificationRepository,
                                          CustomerRepository customerRepository) {
        this.senderFactory = senderFactory;
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
    }

    public void sendBookingConfirmation(Appointment appointment) {
        dispatchAll(appointment, NotificationType.APPOINTMENT_CONFIRMATION,
                "confirmação de agendamento");
    }

    public void sendCancellation(Appointment appointment) {
        dispatchAll(appointment, NotificationType.CANCELLATION,
                "cancelamento de agendamento");
    }

    /**
     * Busca as preferências de notificação do cliente autenticado.
     *
     * @param customerId identificador do cliente
     * @return canais atualmente habilitados pelo cliente
     */
    @Transactional
    public NotificationPreferencesResponse getMyPreferences(Long customerId) {
        Customer customer = loadCustomer(customerId);
        return new NotificationPreferencesResponse(
                Set.copyOf(customer.getNotificationPreferences()));
    }

    /**
     * Substitui as preferências de notificação do cliente.
     *
     * <p>Quando a requisição não possui canais, o cliente fica sem canais de
     * notificação habilitados.</p>
     *
     * @param customerId identificador do cliente
     * @param request novos canais escolhidos pelo cliente
     * @return preferências atualizadas
     */
    @Transactional
    public NotificationPreferencesResponse updateMyPreferences(
            Long customerId, NotificationPreferencesRequest request) {
        Customer customer = loadCustomer(customerId);
        Set<NotificationChannel> channels = request.channels() == null || request.channels().isEmpty()
                ? EnumSet.noneOf(NotificationChannel.class)
                : EnumSet.copyOf(request.channels());
        customer.setNotificationPreferences(channels);
        customerRepository.persist(customer);
        return new NotificationPreferencesResponse(Set.copyOf(channels));
    }

    private Customer loadCustomer(Long customerId) {
        Customer customer = customerRepository.getById(customerId);
        if (customer == null) {
            throw new NotFoundException("Cliente não encontrado.");
        }
        return customer;
    }

    /**
     * Envia uma notificação para todos os canais habilitados pelo cliente.
     *
     * <p>O envio respeita a configuração global de notificações e registra cada
     * tentativa individualmente.</p>
     */
    @Transactional
    public void dispatchAll(Appointment appointment, NotificationType type, String kind) {
        if (!SystemConfiguration.getInstance().isEnabled("module.notifications.enabled")) {
            log.debugf("Notificações desligadas pela feature flag - %s não enviada(s).", kind);
            return;
        }
        Customer customer = appointment.getCustomer();
        if (customer == null || customer.getNotificationPreferences() == null
                || customer.getNotificationPreferences().isEmpty()) {
            log.debugf("Cliente sem canais de notificação configurados - %s não enviada.", kind);
            return;
        }
        for (NotificationChannel channel : customer.getNotificationPreferences()) {
            NotificationSender sender = senderFactory.resolve(channel);
            SendResult result = sender.send(appointment);
            persistAttempt(customer, appointment, type, channel, result);
            if (!result.success()) {
                log.warnf("Falha ao enviar %s via %s: %s",
                        kind, channel, result.errorMessage());
            }
        }
    }

    /**
     * Registra uma tentativa de envio de notificação.
     *
     * <p>Falhas também são persistidas para manter o histórico do que o sistema
     * tentou enviar.</p>
     */
    private void persistAttempt(Customer customer, Appointment appointment,
                                NotificationType type, NotificationChannel channel,
                                SendResult result) {
        String message = result.formattedMessage() != null
                ? result.formattedMessage()
                : "(mensagem não formatada - destinatário inválido)";
        Notification record = new Notification(customer, type, channel, message);
        record.setAppointment(appointment);
        if (result.success()) {
            record.setStatus(NotificationStatus.SENT);
            record.setSentAt(LocalDateTime.now());
        } else {
            record.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.persist(record);
    }
}