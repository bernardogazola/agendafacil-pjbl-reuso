package br.pucpr.agendafacil.adapter.out.persistence.notification.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.UserJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentJpaEntity;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.domain.notification.NotificationStatus;
import br.pucpr.agendafacil.domain.notification.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma notificação no banco de dados.
 *
 * <p>Esta classe armazena o destinatário, o canal de envio, o tipo da
 * notificação, a mensagem, o status, a data de envio e a data de criação do
 * registro.</p>
 *
 * <p>O vínculo com {@link AppointmentJpaEntity} é opcional, pois algumas
 * notificações, como mensagens promocionais, não estão associadas diretamente a
 * um agendamento.</p>
 *
 * <p>O destinatário aponta para {@link UserJpaEntity}, permitindo registrar
 * notificações para diferentes tipos de usuário.</p>
 */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private AppointmentJpaEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserJpaEntity recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 16)
    private NotificationChannel channel;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Define a data de criação antes da primeira persistência.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public AppointmentJpaEntity getAppointment() {
        return appointment;
    }
    public void setAppointment(AppointmentJpaEntity appointment) {
        this.appointment = appointment;
    }

    public UserJpaEntity getRecipient() {
        return recipient;
    }
    public void setRecipient(UserJpaEntity recipient) {
        this.recipient = recipient;
    }

    public NotificationType getType() {
        return type;
    }
    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}