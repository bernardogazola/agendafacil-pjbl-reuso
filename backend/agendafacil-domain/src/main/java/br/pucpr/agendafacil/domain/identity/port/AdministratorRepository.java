package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Administrator;

import java.util.List;

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

    /**
     * Atualiza um administrador existente.
     *
     * @param administrator administrador com os dados atualizados
     * @return administrador atualizado
     */
    Administrator update(Administrator administrator);

    /**
     * Lista os administradores cadastrados.
     *
     * <p>Quando {@code activeOnly} for {@code true}, retorna apenas
     * administradores ativos.</p>
     *
     * @param activeOnly indica se a busca deve considerar apenas administradores ativos
     * @return lista de administradores encontrados
     */
    List<Administrator> listAll(boolean activeOnly);
}