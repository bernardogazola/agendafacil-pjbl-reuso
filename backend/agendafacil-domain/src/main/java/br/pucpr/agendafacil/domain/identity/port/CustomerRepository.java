package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Customer;

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
    Customer findById(Long id);

    /**
     * Persiste um novo cliente.
     *
     * @param customer cliente a ser salvo
     */
    void persist(Customer customer);
}