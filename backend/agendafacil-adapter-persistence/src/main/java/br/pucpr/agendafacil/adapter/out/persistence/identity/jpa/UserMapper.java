package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import br.pucpr.agendafacil.domain.identity.User;

/**
 * Mapper responsável por converter usuários JPA para o modelo de domínio.
 *
 * <p>Como {@link UserJpaEntity} possui subtipos concretos, este mapper identifica
 * se a entidade representa um cliente ou um administrador e delega a conversão
 * para o mapper específico.</p>
 *
 * <p>A conversão no sentido contrário não fica nesta classe, pois os fluxos de
 * cadastro já conhecem o subtipo que está sendo criado.</p>
 */
public final class UserMapper {

    private UserMapper() {}

    /**
     * Converte uma entidade JPA de usuário para o subtipo correspondente no domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return usuário de domínio correspondente ou {@code null} quando a entrada for nula
     * @throws IllegalStateException se o subtipo da entidade JPA não for reconhecido
     */
    public static User toDomain(UserJpaEntity jpa) {
        return switch (jpa) {
            case null -> null;
            case CustomerJpaEntity customer -> CustomerMapper.toDomain(customer);
            case AdministratorJpaEntity administrator -> AdministratorMapper.toDomain(administrator);
            default -> throw new IllegalStateException(
                    "Subtipo de UserJpaEntity desconhecido: " + jpa.getClass().getName());
        };
    }
}
