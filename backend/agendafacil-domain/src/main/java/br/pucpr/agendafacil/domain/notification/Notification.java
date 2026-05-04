package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma notificação enviada ou registrada para um {@link User}.
 *
 * <p>A notificação guarda o destinatário, o canal de envio, o tipo da mensagem,
 * o conteúdo enviado, o status atual e, quando existir, o agendamento
 * relacionado.</p>
 *
 * <p>O vínculo com {@link Appointment} é opcional, pois algumas notificações,
 * como mensagens promocionais, não estão ligadas diretamente a uma reserva.</p>
 */
public class Notification {

    private Long id;

    private Appointment appointment;

    @NotNull
    private User recipient;

    @NotNull(message = "O tipo da notificação é obrigatório")
    private NotificationType type;

    @NotNull(message = "O canal da notificação é obrigatório")
    private NotificationChannel channel;

    @NotBlank(message = "A mensagem é obrigatória")
    @Size(max = 1000)
    private String message;

    @NotNull
    private NotificationStatus status = NotificationStatus.PENDING;

    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(User recipient, NotificationType type, NotificationChannel channel, String message) {
        this.recipient = recipient;
        this.type = type;
        this.channel = channel;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", type=" + type + ", channel=" + channel + ", status=" + status + "}";
    }
}
