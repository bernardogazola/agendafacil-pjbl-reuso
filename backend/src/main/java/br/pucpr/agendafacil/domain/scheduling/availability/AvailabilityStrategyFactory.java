package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory responsável por escolher a estratégia de disponibilidade adequada
 * para cada tipo de estabelecimento.
 *
 * <p>Esta classe complementa o uso do padrão Strategy: em vez de espalhar
 * condicionais pelo código da agenda, a escolha da implementação fica
 * centralizada aqui.</p>
 *
 * <p>Com isso, {@code Schedule} pode apenas pedir uma {@link AvailabilityStrategy}
 * e executar o cálculo, sem precisar saber se o negócio usa horários fixos,
 * intervalos configuráveis ou atendimento sob demanda.</p>
 */
@ApplicationScoped
public class AvailabilityStrategyFactory {

    private static final int DEFAULT_SLOT_MINUTES = 30;

    /**
     * Retorna a estratégia de disponibilidade correspondente à categoria do
     * estabelecimento.
     */
    public AvailabilityStrategy resolve(Business business) {
        return switch (business.getCategory()) {
            case BARBER_SHOP, SALON, OTHER -> new FixedHoursAvailability();
            case CLINIC, AESTHETICS, PSYCHOLOGIST -> new SlotBasedAvailability(DEFAULT_SLOT_MINUTES);
            case PERSONAL_TRAINER -> new OnDemandAvailability();
        };
    }
}
