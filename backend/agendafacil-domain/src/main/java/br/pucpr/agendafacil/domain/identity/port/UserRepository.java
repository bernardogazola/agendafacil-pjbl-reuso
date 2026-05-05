package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.User;

import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link User}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: os serviços
 * de aplicação dependem dela para consultar usuários, sem conhecer os detalhes
 * da tecnologia usada para armazenar os dados.</p>
 */
public interface UserRepository {

    /**
     * Busca um usuário pelo e-mail.
     *
     * @param email e-mail usado na busca
     * @return usuário encontrado, quando existir
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se já existe um usuário cadastrado com o e-mail informado.
     *
     * @param email e-mail que será verificado
     * @return {@code true} se já existir um usuário com esse e-mail
     */
    boolean existsByEmail(String email);
}