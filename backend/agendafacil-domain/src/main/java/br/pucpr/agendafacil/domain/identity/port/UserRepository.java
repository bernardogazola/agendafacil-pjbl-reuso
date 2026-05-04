package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.User;

import java.util.Optional;

/**
 * Porta de saída - repositório de {@link User}.
 */
public interface UserRepository {

    /**
     * Procura um usuário pelo e-mail.
     * @param email único em {@code users.email}.
     * @return {@link Optional} contendo o usuário se encontrado, caso contrário {@link Optional} vázio.
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se já existe algum usuário com o e-mail informado.
     * @param email e-mail informado.
     */
    boolean existsByEmail(String email);
}