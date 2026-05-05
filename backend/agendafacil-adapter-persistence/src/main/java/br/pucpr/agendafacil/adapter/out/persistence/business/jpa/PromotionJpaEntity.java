package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade JPA que representa uma campanha promocional de um estabelecimento.
 *
 * <p>A promoção pode aplicar desconto percentual ou valor fixo dentro de uma
 * janela de validade. Ela também pode ser associada a serviços específicos por
 * meio da tabela de relacionamento.</p>
 *
 * <p>O campo {@code active} permite desativar a promoção sem remover seu
 * histórico do banco de dados.</p>
 */
@Entity
@Table(name = "promotions")
public class PromotionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessJpaEntity business;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_offered_services",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "offered_service_id")
    )
    private Set<OfferedServiceJpaEntity> eligibleServices = new HashSet<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public BusinessJpaEntity getBusiness() {
        return business;
    }
    public void setBusiness(BusinessJpaEntity business) {
        this.business = business;
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

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }
    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<OfferedServiceJpaEntity> getEligibleServices() {
        return eligibleServices;
    }
    public void setEligibleServices(Set<OfferedServiceJpaEntity> eligibleServices) {
        this.eligibleServices = eligibleServices;
    }
}