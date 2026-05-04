package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Política de cancelamento com cobrança de multa após um prazo limite.
 *
 * <p>O cancelamento é sempre permitido. Porém, quando ele ocorre com menos de
 * {@code deadlineHours} horas de antecedência, é calculada uma multa com base
 * no valor pago pelo agendamento.</p>
 *
 * <p>Essa regra é útil para estabelecimentos que não querem bloquear o
 * cancelamento, mas precisam reduzir o prejuízo causado por cancelamentos
 * tardios.</p>
 */
public class FeeBasedCancellation implements CancellationPolicy {

    private final int deadlineHours;
    private final BigDecimal feePercentage;

    public FeeBasedCancellation(int deadlineHours, BigDecimal feePercentage) {
        this.deadlineHours = deadlineHours;
        this.feePercentage = feePercentage;
    }

    @Override
    public CancellationResult evaluate(Appointment appointment, LocalDateTime moment) {
        long hoursUntil = Duration.between(moment, appointment.getScheduledAt()).toHours();
        if (hoursUntil >= deadlineHours) {
            return CancellationResult.allow();
        }
        BigDecimal fee = appointment.getPricePaid()
                .multiply(feePercentage)
                .setScale(2, RoundingMode.HALF_UP);
        return CancellationResult.allowWithFee(fee);
    }
}
