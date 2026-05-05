package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.OfferedServiceJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.OfferedServiceMapper;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso aos serviços oferecidos.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link OfferedServiceMapper}.</p>
 */
@ApplicationScoped
public class OfferedServicePanacheRepository implements OfferedServiceRepository,
        PanacheRepository<OfferedServiceJpaEntity> {

    private final EntityManager em;

    @Inject
    public OfferedServicePanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<OfferedService> findActiveById(Long id) {
        return find("id = ?1 and active = true", id)
                .firstResultOptional()
                .map(OfferedServiceMapper::toDomain);
    }

    @Override
    public List<OfferedService> findByBusinessId(Long businessId) {
        return list("business.id", businessId).stream()
                .map(OfferedServiceMapper::toDomain)
                .toList();
    }

    @Override
    public List<OfferedService> findActiveByBusinessId(Long businessId) {
        return list("business.id = ?1 and active = true", businessId).stream()
                .map(OfferedServiceMapper::toDomain)
                .toList();
    }

    @Override
    public void persist(OfferedService service) {
        OfferedServiceJpaEntity jpa = OfferedServiceMapper.toJpa(service, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        service.setId(jpa.getId());
    }
}