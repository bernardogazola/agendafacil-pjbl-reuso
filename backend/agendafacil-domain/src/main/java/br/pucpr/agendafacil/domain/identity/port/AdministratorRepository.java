package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Administrator;

/**
 * Repositório responsável pelo acesso aos dados de {@link Administrator}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: os serviços
 * de aplicação dependem dela para consultar e persistir administradores, sem
 * conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface AdministratorRepository {

    /**
     * Busca um administrador pelo id.
     *
     * @param id identificador do administrador
     * @return o administrador encontrado ou {@code null} quando não existir
     */
    Administrator getById(Long id);

    /**
     * Persiste um novo administrador.
     *
     * @param administrator administrador a ser salvo
     */
    void persist(Administrator administrator);
}