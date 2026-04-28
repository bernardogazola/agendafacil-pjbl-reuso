package br.pucpr.agendafacil.domain.business;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Representa uma campanha promocional de um {@link Business}.
 *
 * <p>A promoção pode aplicar desconto percentual ou valor fixo sobre serviços
 * elegíveis dentro de um período de validade.</p>
 *
 * <p>Quando a lista de serviços elegíveis estiver preenchida, a promoção deve
 * ser considerada apenas para esses serviços. O campo {@code active} permite
 * desativar a campanha sem remover seu histórico.</p>
 */
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @NotBlank(message = "O nome da promoção é obrigatório")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @NotNull(message = "A data inicial da promoção é obrigatória")
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @NotNull(message = "A data final da promoção é obrigatória")
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_offered_services",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "offered_service_id")
    )
    private Set<OfferedService> eligibleServices = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    public Promotion() {
    }

    public Promotion(Business business, String name, LocalDate validFrom, LocalDate validTo) {
        this.business = business;
        this.name = name;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
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

    public Set<OfferedService> getEligibleServices() {
        return eligibleServices;
    }

    public void setEligibleServices(Set<OfferedService> eligibleServices) {
        this.eligibleServices = eligibleServices;
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

    /**
     * Indica se a data informada está dentro do período de validade da promoção.
     *
     * <p>Este método verifica apenas a janela de datas. A situação ativa ou inativa
     * da promoção deve ser avaliada separadamente por quem chama o método.</p>
     *
     * @param date data que será verificada
     * @return {@code true} se a data estiver dentro do período de validade
     */
    public boolean isValidOn(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Promotion{id=" + id + ", name='" + name + "', validFrom=" + validFrom
                + ", validTo=" + validTo + "}";
    }
}

