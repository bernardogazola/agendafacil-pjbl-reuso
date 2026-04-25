package br.pucpr.agendafacil.domain.identity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Cliente final, pessoa física que agenda serviços.
 */
@Entity
@Table(name = "customers")
public class Customer extends User {

    @Column(name = "birth_date")
    private LocalDate birthDate;

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
}
