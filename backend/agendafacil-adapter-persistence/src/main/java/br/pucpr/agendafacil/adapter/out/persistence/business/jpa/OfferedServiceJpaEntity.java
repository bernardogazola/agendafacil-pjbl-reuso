package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um serviço oferecido por um estabelecimento.
 *
 * <p>Esta classe armazena os dados do serviço, como nome, descrição, preço
 * base, duração estimada, categoria, política de precificação e situação
 * ativa.</p>
 *
 * <p>Cada serviço pertence a um {@link BusinessJpaEntity}. As datas de criação
 * e atualização são controladas pelos métodos de ciclo de vida da entidade.</p>
 */
@Entity
@Table(name = "offered_services")
public class OfferedServiceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_policy_type", nullable = false, length = 30)
    private PricingPolicyType pricingPolicyType = PricingPolicyType.FIXED;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessJpaEntity business;

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
    public void setActive(boolean active) {
        this.active = active;
    }

    public BusinessJpaEntity getBusiness() {
        return business;
    }
    public void setBusiness(BusinessJpaEntity business) {
        this.business = business;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}