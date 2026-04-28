package br.pucpr.agendafacil.domain.business;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicy;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa um serviço oferecido por um {@link Business}.
 *
 * <p>O serviço possui nome, descrição, preço base, duração estimada, categoria
 * interna e situação ativa ou inativa.</p>
 *
 * <p>O preço base serve como referência para o cálculo do valor final do
 * agendamento. Esse cálculo pode variar conforme a política de precificação
 * configurada para o serviço.</p>
 */
@Entity
@Table(name = "offered_services")
public class OfferedService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull(message = "O preço base é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço base deve ser positivo")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @NotNull(message = "A duração é obrigatória")
    @Positive(message = "A duração deve ser positiva")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Size(max = 50)
    @Column(length = 50)
    private String category;

    /**
     * Política de precificação configurada para este serviço.
     *
     * <p>Esse valor é usado para escolher a implementação de
     * {@link PricingPolicy} responsável por calcular o preço final do
     * agendamento.</p>
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_policy_type", nullable = false, length = 30)
    private PricingPolicyType pricingPolicyType = PricingPolicyType.FIXED;

    @Column(nullable = false)
    private boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OfferedService() {
    }

    public OfferedService(String name, BigDecimal basePrice, Integer durationMinutes, Business business) {
        this.name = name;
        this.basePrice = basePrice;
        this.durationMinutes = durationMinutes;
        this.business = business;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public PricingPolicyType getPricingPolicyType() {
        return pricingPolicyType;
    }

    public void setPricingPolicyType(PricingPolicyType pricingPolicyType) {
        this.pricingPolicyType = pricingPolicyType;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Calcula o preço final do serviço usando a política de precificação informada.
     *
     * <p>Este método atua como o contexto do padrão Strategy. A entidade não
     * conhece a regra concreta de cálculo, ela apenas delega a decisão para a
     * {@link PricingPolicy} recebida como parâmetro.</p>
     *
     * <p>Após o cálculo, o valor é convertido para {@link BigDecimal} e arredondado
     * para duas casas decimais.</p>
     *
     * @param appointment agendamento usado no cálculo, quando a política precisar
     *                    de informações como duração ou horário
     * @param policy política de precificação aplicada ao serviço
     * @return preço final do serviço, arredondado para duas casas decimais
     */
    public BigDecimal calculateFinalPrice(Appointment appointment, PricingPolicy policy) {
        double raw = policy.calculate(this, appointment);
        return BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferedService other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OfferedService{id=" + id + ", name='" + name + "', basePrice=" + basePrice + "}";
    }
}
