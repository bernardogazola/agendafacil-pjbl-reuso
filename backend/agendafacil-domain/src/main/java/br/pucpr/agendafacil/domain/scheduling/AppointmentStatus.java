package br.pucpr.agendafacil.domain.scheduling;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados possíveis de um agendamento.
 *
 * <p>O enum também concentra as transições válidas entre estados. Com isso, a
 * regra sobre quais mudanças de status são permitidas fica em um único lugar do
 * domínio.</p>
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    CANCELED,
    COMPLETED,
    NO_SHOW;

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS = Map.of(
            SCHEDULED, EnumSet.of(CONFIRMED, CANCELED),
            CONFIRMED, EnumSet.of(COMPLETED, CANCELED, NO_SHOW),
            CANCELED, EnumSet.noneOf(AppointmentStatus.class),
            COMPLETED, EnumSet.noneOf(AppointmentStatus.class),
            NO_SHOW, EnumSet.noneOf(AppointmentStatus.class)
    );

    /**
     * Verifica se este estado pode ser alterado para o estado informado.
     *
     * @param target estado de destino
     * @return {@code true} se a transição for permitida; {@code false} caso contrário
     */
    public boolean canTransitionTo(AppointmentStatus target) {
        if (target == null) {
            return false;
        }
        return TRANSITIONS.get(this).contains(target);
    }
}
