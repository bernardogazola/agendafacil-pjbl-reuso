package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.LocalDateTime;

/**
 * Política de cancelamento livre.
 *
 * <p>Nesta política, qualquer solicitação de cancelamento é aceita sem cobrança
 * de multa.</p>
 *
 * <p>É o caso mais simples de {@link CancellationPolicy}, usado quando o
 * estabelecimento não aplica restrições ao cancelamento.</p>
 */
public class FreeCancellation implements CancellationPolicy {

    @Override
    public CancellationResult evaluate(Appointment appointment, LocalDateTime moment) {
        return CancellationResult.allow();
    }
}
