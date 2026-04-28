package br.pucpr.agendafacil.domain.scheduling.cancellation;

/**
 * Tipos de política de cancelamento disponíveis para um estabelecimento.
 *
 * <p>Cada valor representa uma forma diferente de avaliar o cancelamento de um
 * agendamento. A conversão deste enum para uma implementação concreta de
 * {@link CancellationPolicy} é feita pela {@link CancellationPolicyFactory}.</p>
 */
public enum CancellationPolicyType {
    FREE,
    DEADLINE,
    FEE_BASED
}
