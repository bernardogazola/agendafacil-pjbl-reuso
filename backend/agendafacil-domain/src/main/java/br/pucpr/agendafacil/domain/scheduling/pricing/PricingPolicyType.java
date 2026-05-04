package br.pucpr.agendafacil.domain.scheduling.pricing;

/**
 * Tipos de política de precificação disponíveis para um serviço.
 *
 * <p>Cada valor representa uma forma diferente de calcular o preço de um
 * agendamento. A conversão deste enum para uma implementação concreta de
 * {@link PricingPolicy} é feita pela {@link PricingPolicyFactory}.</p>
 */
public enum PricingPolicyType {
    FIXED,
    DURATION_BASED,
    DISCOUNTED
}

