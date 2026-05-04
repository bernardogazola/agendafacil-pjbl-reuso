package br.pucpr.agendafacil.domain.identity;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Cliente final, pessoa física que agenda serviços.
 */
public class Customer extends User {

    private LocalDate birthDate;

    private Set<NotificationChannel> notificationPreferences = EnumSet.noneOf(NotificationChannel.class);

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
