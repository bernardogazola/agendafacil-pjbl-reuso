package br.pucpr.agendafacil.domain.business;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicy;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class OfferedService {

    private Long id;

    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "O preço base é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço base deve ser positivo")
    private BigDecimal basePrice;

    @NotNull(message = "A duração é obrigatória")
    @Positive(message = "A duração deve ser positiva")
    private Integer durationMinutes;

    @Size(max = 50)
    private String category;

    /**
     * Política de precificação configurada para este serviço.
     *
     * <p>Esse valor é usado para escolher a implementação de
     * {@link PricingPolicy} responsável por calcular o preço final do
     * agendamento.</p>
     */
    @NotNull
    private PricingPolicyType pricingPolicyType = PricingPolicyType.FIXED;

    private boolean active = true;

    @NotNull
    private Business business;

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
