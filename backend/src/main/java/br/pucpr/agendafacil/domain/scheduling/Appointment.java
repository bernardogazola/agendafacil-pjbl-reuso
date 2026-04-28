package br.pucpr.agendafacil.domain.scheduling;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicy;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationResult;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa o agendamento de um {@link Customer} em uma {@link Business} para
 * um {@link OfferedService}.
 *
 * <p>Esta entidade guarda as principais informações da reserva, como cliente,
 * estabelecimento, serviço, horário, duração estimada, preço pago, status e
 * observações.</p>
 *
 * <p>Os campos {@code estimatedDurationMinutes} e {@code pricePaid} guardam os
 * valores definidos no momento da reserva. Assim, o histórico do agendamento não
 * muda caso o serviço tenha seu preço ou duração alterados depois.</p>
 *
 * <p>Em alguns tipos de estabelecimento, o processamento pode ajustar dados do
 * agendamento antes da persistência. Por exemplo, serviços de estética podem
 * aumentar a duração efetiva para incluir um intervalo pós-procedimento, e
 * clínicas podem gerar uma referência de ficha clínica.</p>
 */
@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointments_customer", columnList = "customer_id"),
                @Index(name = "idx_appointments_business_scheduled",
                        columnList = "business_id,scheduled_at")
        }
)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offered_service_id", nullable = false)
    private OfferedService offeredService;

    @NotNull(message = "A data/hora do agendamento é obrigatória")
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @NotNull
    @Positive(message = "A duração estimada deve ser positiva")
    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    @NotNull
    @Column(name = "price_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Size(max = 500)
    @Column(length = 500)
    private String notes;

    @Size(max = 64)
    @Column(name = "clinical_record_ref", length = 64)
    private String clinicalRecordRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Appointment() {
    }

    public Appointment(Customer customer, Business business, OfferedService offeredService,
                       LocalDateTime scheduledAt, Integer estimatedDurationMinutes, BigDecimal pricePaid) {
        this.customer = customer;
        this.business = business;
        this.offeredService = offeredService;
        this.scheduledAt = scheduledAt;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.pricePaid = pricePaid;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public OfferedService getOfferedService() {
        return offeredService;
    }

    public void setOfferedService(OfferedService offeredService) {
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

    /**
     * Indica se o agendamento ainda está ativo.
     *
     * <p>São considerados ativos os agendamentos com status
     * {@link AppointmentStatus#SCHEDULED} ou {@link AppointmentStatus#CONFIRMED}.</p>
     */
    public boolean isActive() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    /**
     * Cancela o agendamento usando a política de cancelamento informada.
     *
     * <p>Este método atua como contexto do padrão Strategy: a regra de
     * cancelamento fica na {@link CancellationPolicy}, enquanto a entidade
     * apenas aplica o resultado e atualiza o status quando o cancelamento é
     * permitido.</p>
     *
     * <p>Antes de alterar o status, a transição para
     * {@link AppointmentStatus#CANCELED} é validada pela máquina de estados de
     * {@link AppointmentStatus}.</p>
     *
     * @param moment instante em que o cancelamento foi solicitado
     * @param policy política usada para avaliar o cancelamento
     * @return resultado da avaliação da política
     * @throws IllegalStateException se a política permitir o cancelamento, mas
     *         o status atual não aceitar a transição para cancelado
     */
    public CancellationResult cancel(LocalDateTime moment, CancellationPolicy policy) {
        CancellationResult result = policy.evaluate(this, moment);
        if (result.allowed()) {
            if (!this.status.canTransitionTo(AppointmentStatus.CANCELED)) {
                throw new IllegalStateException(
                        "Agendamento não pode ser cancelado no status atual: " + this.status);
            }
            this.status = AppointmentStatus.CANCELED;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", scheduledAt=" + scheduledAt + ", status=" + status + "}";
    }
}
