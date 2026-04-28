package br.pucpr.agendafacil.domain.identity;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente final, pessoa física que agenda serviços.
 */
@Entity
@Table(name = "customers")
public class Customer extends User {

    @Column(name = "birth_date")
    private LocalDate birthDate;

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

    public List<Appointment> getAppointments() {
        return appointments;
    }
}
