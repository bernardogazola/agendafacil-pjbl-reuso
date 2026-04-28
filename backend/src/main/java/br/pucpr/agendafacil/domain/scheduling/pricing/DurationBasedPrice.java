package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Política de preço proporcional à duração do serviço.
 *
 * <p>Nesta política, o preço é calculado multiplicando a duração do serviço
 * pela taxa cobrada por minuto.</p>
 *
 * <p>Ela é útil para serviços em que o tempo de atendimento influencia
 * diretamente o valor cobrado.</p>
 */
public class DurationBasedPrice implements PricingPolicy {

    private final double ratePerMinute;

    public DurationBasedPrice(double ratePerMinute) {
        this.ratePerMinute = ratePerMinute;
    }

    @Override
    public double calculate(OfferedService service, Appointment appointment) {
        return service.getDurationMinutes() * ratePerMinute;
    }
}
