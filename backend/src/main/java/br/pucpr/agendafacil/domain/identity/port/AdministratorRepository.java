package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Administrator;

/**
 * Porta de saída - repositório de {@link Administrator}.
 */
public interface AdministratorRepository {

    /**
     * Procura um administrador pelo id.
     * @param id identificador único.
     * @return {@link Administrator} ou {@code null} quando não encontrado.
     */
    Administrator findById(Long id);

    /**
     * Persiste um novo administrador.
     * @param administrator novo administrador.
     */
    void persist(Administrator administrator);
}

