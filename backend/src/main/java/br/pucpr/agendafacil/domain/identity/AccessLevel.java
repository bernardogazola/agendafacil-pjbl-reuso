package br.pucpr.agendafacil.domain.identity;

/**
 * Nível de acesso de um administrador no sistema.
 * <p>
 * {@link #SUPER_ADMIN} gerencia toda a plataforma (uso operacional).
 * {@link #BUSINESS_ADMIN} responde apenas pela própria empresa cadastrada.
 */
public enum AccessLevel {
    SUPER_ADMIN,
    BUSINESS_ADMIN
}
