package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.Customer;

/**
 * Porta de saída - repositório de {@link Customer}.
 */
public interface CustomerRepository {

    /**
     * Procura um cliente pelo id.
     * @param id identificador único.
     * @return {@link Customer} ou {@code null} quando não encontrado.
     */
    Customer findById(Long id);

    /**
     * Persiste um novo cliente.
     * @param customer novo cliente.
     */
    void persist(Customer customer);
}
