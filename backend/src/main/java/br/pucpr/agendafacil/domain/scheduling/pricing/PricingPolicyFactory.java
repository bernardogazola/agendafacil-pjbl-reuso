package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory responsável por escolher a política de precificação de um serviço.
 *
 * <p>Esta classe centraliza a escolha da implementação concreta de
 * {@link PricingPolicy}. Assim, o código que realiza o agendamento pode
 * depender apenas da interface, sem precisar verificar diretamente o tipo de
 * precificação configurado no serviço.</p>
 *
 * <p>Quando uma nova política de preço for adicionada, a escolha da nova
 * implementação deve ser concentrada nesta factory.</p>
 */
@ApplicationScoped
public class PricingPolicyFactory {

    private static final double DEFAULT_DISCOUNT = 0.10;

    /**
     * Retorna a política de precificação configurada para o serviço.
     */
    public PricingPolicy resolve(OfferedService service) {
        return switch (service.getPricingPolicyType()) {
            case FIXED -> new FixedPrice();
            case DURATION_BASED -> new DurationBasedPrice(defaultRatePerMinute(service));
            case DISCOUNTED -> new DiscountedPrice(new FixedPrice(), DEFAULT_DISCOUNT);
        };
    }

    /**
     * Calcula uma taxa padrão por minuto a partir do preço base e da duração do
     * serviço.
     *
     * <p>Quando a duração não estiver preenchida ou for inválida, é usado o
     * valor {@code 1} para evitar divisão por zero.</p>
     */
    private double defaultRatePerMinute(OfferedService s) {
        int duration = s.getDurationMinutes() == null || s.getDurationMinutes() <= 0
                ? 1 : s.getDurationMinutes();
        return s.getBasePrice().doubleValue() / duration;
    }
}
