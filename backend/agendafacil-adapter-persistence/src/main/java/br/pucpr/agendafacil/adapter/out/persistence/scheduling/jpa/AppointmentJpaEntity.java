package br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.OfferedServiceJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um agendamento no banco de dados.
 *
 * <p>Esta classe armazena os dados persistidos do agendamento, incluindo
 * cliente, estabelecimento, serviço, horário, duração estimada, preço pago,
 * status, observações e referência de ficha clínica quando existir.</p>
 *
 * <p>O índice por estabelecimento e horário ajuda nas consultas usadas para
 * verificar conflitos de agenda. As datas de criação e atualização são
 * controladas pelos métodos de ciclo de vida da entidade.</p>
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointments_business_scheduled",
                columnList = "business_id, scheduled_at")
})
public class AppointmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessJpaEntity business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offered_service_id", nullable = false)
    private OfferedServiceJpaEntity offeredService;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "clinical_record_ref", length = 64)
    private String clinicalRecordRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Define as datas de criação e atualização antes da primeira persistência.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Atualiza a data de modificação antes de salvar alterações na entidade.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public CustomerJpaEntity getCustomer() {
        return customer;
    }
    public void setCustomer(CustomerJpaEntity customer) {
        this.customer = customer;
    }

    public BusinessJpaEntity getBusiness() {
        return business;
    }
    public void setBusiness(BusinessJpaEntity business) {
        this.business = business;
    }

    public OfferedServiceJpaEntity getOfferedService() {
        return offeredService;
    }
    public void setOfferedService(OfferedServiceJpaEntity offeredService) {
        this.offeredService = offeredService;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }
    public void setPricePaid(BigDecimal pricePaid) {
        this.pricePaid = pricePaid;
    }

    public AppointmentStatus getStatus() {
        return status;
    }
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClinicalRecordRef() {
        return clinicalRecordRef;
    }
    public void setClinicalRecordRef(String clinicalRecordRef) {
        this.clinicalRecordRef = clinicalRecordRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}