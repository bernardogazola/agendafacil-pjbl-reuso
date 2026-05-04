package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Política de preço com desconto aplicado sobre outra política.
 *
 * <p>Esta política calcula primeiro o valor usando uma política base e, em
 * seguida, aplica o percentual de desconto configurado. Dessa forma, o desconto
 * pode ser combinado com diferentes formas de cálculo de preço.</p>
 *
 * <p>Por exemplo, o desconto pode ser aplicado sobre um preço fixo ou sobre um
 * preço calculado por duração, desde que a regra base implemente
 * {@link PricingPolicy}.</p>
 */
public class DiscountedPrice implements PricingPolicy {

    private final PricingPolicy basePolicy;
    private final double discountRate;

    public DiscountedPrice(PricingPolicy basePolicy, double discountRate) {
        this.basePolicy = basePolicy;
        this.discountRate = discountRate;
    }

    @Override
    public double calculate(OfferedService service, Appointment appointment) {
        double gross = basePolicy.calculate(service, appointment);
        return gross * (1 - discountRate);
    }
}

