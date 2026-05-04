package br.pucpr.agendafacil.domain.scheduling.cancellation;

import java.math.BigDecimal;

/**
 * Resultado da avaliação de uma solicitação de cancelamento.
 *
 * <p>O resultado informa se o cancelamento foi permitido, se há multa a ser
 * cobrada e, em caso de recusa, a mensagem que explica o motivo ao usuário.</p>
 */
public record CancellationResult(boolean allowed, BigDecimal fee, String reason) {

    /**
     * Cria um resultado de cancelamento permitido sem cobrança de multa.
     */
    public static CancellationResult allow() {
        return new CancellationResult(true, BigDecimal.ZERO, null);
    }

    /**
     * Cria um resultado de cancelamento permitido com cobrança de multa.
     */
    public static CancellationResult allowWithFee(BigDecimal fee) {
        return new CancellationResult(true, fee, null);
    }

    /**
     * Cria um resultado de cancelamento negado com uma justificativa.
     */
    public static CancellationResult deny(String reason) {
        return new CancellationResult(false, BigDecimal.ZERO, reason);
    }
}
