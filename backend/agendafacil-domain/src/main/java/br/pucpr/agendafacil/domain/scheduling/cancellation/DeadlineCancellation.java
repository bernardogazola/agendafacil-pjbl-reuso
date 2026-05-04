package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Política de cancelamento baseada em antecedência mínima.
 *
 * <p>O cancelamento só é permitido quando ainda faltam pelo menos
 * {@code minHours} horas para o horário agendado. Caso contrário, o pedido é
 * negado com uma mensagem explicativa.</p>
 *
 * <p>Essa regra é útil para serviços em que um cancelamento muito próximo do
 * horário dificulta o reaproveitamento da agenda.</p>
 */
public class DeadlineCancellation implements CancellationPolicy {

    private final int minHours;

    public DeadlineCancellation(int minHours) {
        this.minHours = minHours;
    }

    @Override
    public CancellationResult evaluate(Appointment appointment, LocalDateTime moment) {
        long hoursUntil = Duration.between(moment, appointment.getScheduledAt()).toHours();
        if (hoursUntil >= minHours) {
            return CancellationResult.allow();
        }
        return CancellationResult.deny(
                "Cancelamento permitido apenas com mais de " + minHours
                        + " horas de antecedência.");
    }
}
