package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerMapper;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Repositório Panache responsável pelo acesso aos dados de {@link Customer}.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link CustomerMapper}.</p>
 */
@ApplicationScoped
public class CustomerPanacheRepository implements CustomerRepository,
        PanacheRepository<CustomerJpaEntity> {

    private final EntityManager em;

    @Inject
    public CustomerPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Customer getById(Long id) {
        return findByIdOptional(id)
                .map(CustomerMapper::toDomain)
                .orElse(null);
    }

    @Override
    public void persist(Customer customer) {
        CustomerJpaEntity jpa = CustomerMapper.toJpa(customer, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        customer.setId(jpa.getId());
    }
}