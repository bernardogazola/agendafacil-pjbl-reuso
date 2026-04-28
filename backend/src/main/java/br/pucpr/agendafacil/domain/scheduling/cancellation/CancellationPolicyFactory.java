package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.business.Business;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

/**
 * Factory responsável por escolher a política de cancelamento de um
 * estabelecimento.
 *
 * <p>Esta classe centraliza a escolha da implementação concreta de
 * {@link CancellationPolicy}. Assim, o código que executa o cancelamento pode
 * depender apenas da interface, sem precisar fazer verificações sobre o tipo de
 * política configurada.</p>
 *
 * <p>Os valores padrão usados atualmente são 24 horas de antecedência e multa
 * de 30% para políticas que dependem desses parâmetros.</p>
 */
@ApplicationScoped
public class CancellationPolicyFactory {

    private static final int DEFAULT_DEADLINE_HOURS = 24;
    private static final BigDecimal DEFAULT_FEE = new BigDecimal("0.30");

    /**
     * Retorna a política de cancelamento configurada para o estabelecimento.
     */
    public CancellationPolicy resolve(Business business) {
        return switch (business.getCancellationPolicyType()) {
            case FREE -> new FreeCancellation();
            case DEADLINE -> new DeadlineCancellation(DEFAULT_DEADLINE_HOURS);
            case FEE_BASED -> new FeeBasedCancellation(DEFAULT_DEADLINE_HOURS, DEFAULT_FEE);
        };
    }
}

