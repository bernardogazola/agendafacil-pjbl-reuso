package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentJpaEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma avaliação feita por um cliente.
 *
 * <p>Cada avaliação está vinculada a um agendamento e a um cliente. A restrição
 * de unicidade no agendamento impede que a mesma reserva receba mais de uma
 * avaliação.</p>
 *
 * <p>A data de criação é definida automaticamente antes da primeira
 * persistência.</p>
 */
@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private AppointmentJpaEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

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

    public CustomerJpaEntity getCustomer() {
        return customer;
    }
    public void setCustomer(CustomerJpaEntity customer) {
        this.customer = customer;
    }

    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}