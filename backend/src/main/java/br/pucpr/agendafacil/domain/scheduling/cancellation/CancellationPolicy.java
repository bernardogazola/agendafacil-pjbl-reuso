package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.LocalDateTime;

/**
 * Política usada para avaliar se um agendamento pode ser cancelado.
 *
 * <p>Esta interface representa o ponto variável do padrão Strategy: cada
 * estabelecimento pode aplicar uma regra diferente de cancelamento, sem que o
 * código responsável por executar o cancelamento precise conhecer os detalhes
 * dessa regra.</p>
 *
 * <p>A avaliação sempre retorna um {@link CancellationResult}, indicando se o
 * cancelamento foi permitido, se existe multa e, quando necessário, o motivo da
 * recusa.</p>
 */
public interface CancellationPolicy {

    CancellationResult evaluate(Appointment appointment, LocalDateTime moment);
}
