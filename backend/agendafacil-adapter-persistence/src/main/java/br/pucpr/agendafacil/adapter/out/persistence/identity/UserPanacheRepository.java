package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.UserJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.UserMapper;
import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.identity.port.UserRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso aos dados de {@link User}.
 *
 * <p>Este repositório trabalha com a entidade base {@link UserJpaEntity} e
 * aproveita a herança JPA para recuperar o subtipo concreto do usuário, como
 * cliente ou administrador.</p>
 *
 * <p>Após a busca, a conversão para o modelo de domínio é feita por
 * {@link UserMapper}.</p>
 */
@ApplicationScoped
public class UserPanacheRepository implements UserRepository, PanacheRepository<UserJpaEntity> {

    @Override
    public Optional<User> findByEmail(String email) {
        return find("email", email)
                .firstResultOptional()
                .map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }
}