package br.pucpr.agendafacil.domain.identity;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Cliente final, pessoa física que agenda serviços.
 */
@Entity
@Table(name = "customers")
public class Customer extends User {

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = NotificationChannel.class)
    @CollectionTable(
            name = "customer_notification_preferences",
            joinColumns = @JoinColumn(name = "customer_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "preference", nullable = false, length = 16)
    private Set<NotificationChannel> notificationPreferences = EnumSet.noneOf(NotificationChannel.class);

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    public Customer() {
    }

    public Customer(String name, String email, String password) {
        super(name, email, password);
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Set<NotificationChannel> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(Set<NotificationChannel> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
}
