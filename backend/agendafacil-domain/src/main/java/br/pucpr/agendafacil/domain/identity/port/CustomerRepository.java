package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Customer;

import java.util.List;

/**
 * Repositório responsável pelo acesso aos dados de {@link Customer}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: os serviços
 * de aplicação dependem dela para consultar e persistir clientes, sem conhecer
 * os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface CustomerRepository {

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador do cliente
     * @return o cliente encontrado ou {@code null} quando não existir
     */
    Customer getById(Long id);

    /**
     * Persiste um novo cliente.
     *
     * @param customer cliente a ser salvo
     */
    void persist(Customer customer);

    /**
     * Atualiza um cliente existente.
     *
     * @param customer cliente com os dados atualizados
     * @return cliente atualizado
     */
    Customer update(Customer customer);

    /**
     * Lista os clientes cadastrados.
     *
     * <p>Quando {@code activeOnly} for {@code true}, retorna apenas clientes
     * ativos.</p>
     *
     * @param activeOnly indica se a busca deve considerar apenas clientes ativos
     * @return lista de clientes encontrados
     */
    List<Customer> listAll(boolean activeOnly);
}