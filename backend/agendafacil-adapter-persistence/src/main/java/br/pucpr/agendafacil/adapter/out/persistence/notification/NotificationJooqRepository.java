package br.pucpr.agendafacil.adapter.out.persistence.notification;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.NotificationsRecord;
import br.pucpr.agendafacil.domain.notification.Notification;
import br.pucpr.agendafacil.domain.notification.port.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.NOTIFICATIONS;

@ApplicationScoped
public class NotificationJooqRepository implements NotificationRepository {

    private final DSLContext dsl;

    @Inject
    public NotificationJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void persist(Notification notification) {
        NotificationsRecord record = dsl.newRecord(NOTIFICATIONS);
        record.setRecipientId(notification.getRecipient().getId());
        record.setType(notification.getType().name());
        record.setChannel(notification.getChannel().name());
        record.setMessage(notification.getMessage());
        record.setStatus(notification.getStatus().name());
        record.setSentAt(notification.getSentAt());
        if (notification.getAppointment() != null && notification.getAppointment().getId() != null) {
            record.setAppointmentId(notification.getAppointment().getId());
        }
        record.store();
        notification.setId(record.getId());
    }
}
