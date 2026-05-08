package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessMapper;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso aos estabelecimentos.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link BusinessMapper}.</p>
 */
@ApplicationScoped
public class BusinessPanacheRepository implements BusinessRepository,
        PanacheRepository<BusinessJpaEntity> {

    private final EntityManager em;

    @Inject
    public BusinessPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Business getById(Long id) {
        return findByIdOptional(id)
                .map(BusinessMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Optional<Business> findActiveById(Long id) {
        return find("id = ?1 and active = true", id)
                .firstResultOptional()
                .map(BusinessMapper::toDomain);
    }

    @Override
    public List<Business> findByOwnerId(Long ownerId) {
        return list("owner.id", ownerId).stream()
                .map(BusinessMapper::toDomain)
                .toList();
    }

    @Override
    public List<Business> listActive() {
        return list("active", true).stream()
                .map(BusinessMapper::toDomain)
                .toList();
    }

    @Override
    public List<Business> listAllAdmin(boolean activeOnly) {
        List<BusinessJpaEntity> entities = activeOnly
                ? list("active", true)
                : listAll();
        return entities.stream()
                .map(BusinessMapper::toDomain)
                .toList();
    }

    @Override
    public void persist(Business business) {
        BusinessJpaEntity jpa = BusinessMapper.toJpa(business, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        business.setId(jpa.getId());
    }

    @Override
    public Business update(Business business) {
        BusinessJpaEntity jpa = BusinessMapper.toJpa(business, em);
        BusinessJpaEntity merged = em.merge(jpa);
        return BusinessMapper.toDomain(merged);
    }
}