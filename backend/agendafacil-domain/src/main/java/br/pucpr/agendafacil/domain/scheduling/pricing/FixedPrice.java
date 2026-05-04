package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Política de preço fixo.
 *
 * <p>Nesta política, o valor cobrado corresponde ao preço base cadastrado no
 * serviço, sem considerar duração real, descontos ou outras variações.</p>
 *
 * <p>É a regra mais simples de {@link PricingPolicy} e pode ser usada como base
 * para outras políticas, como a de desconto.</p>
 */
public class FixedPrice implements PricingPolicy {

    @Override
    public double calculate(OfferedService service, Appointment appointment) {
        return service.getBasePrice().doubleValue();
    }
}
