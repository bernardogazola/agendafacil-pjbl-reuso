package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.PromotionJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.PromotionMapper;
import br.pucpr.agendafacil.domain.business.Promotion;
import br.pucpr.agendafacil.domain.business.port.PromotionRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repositório Panache responsável pelo acesso às campanhas promocionais.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link PromotionMapper}.</p>
 */
@ApplicationScoped
public class PromotionPanacheRepository implements PromotionRepository,
        PanacheRepository<PromotionJpaEntity> {

    private final EntityManager em;

    @Inject
    public PromotionPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Promotion getById(Long id) {
        return findByIdOptional(id)
                .map(PromotionMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Promotion> findByBusinessId(Long businessId) {
        return list("business.id = ?1 order by validFrom desc", businessId).stream()
                .map(PromotionMapper::toDomain)
                .toList();
    }

    @Override
    public void persist(Promotion promotion) {
        PromotionJpaEntity jpa = PromotionMapper.toJpa(promotion, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        promotion.setId(jpa.getId());
    }

    @Override
    public Promotion update(Promotion promotion) {
        PromotionJpaEntity jpa = PromotionMapper.toJpa(promotion, em);
        PromotionJpaEntity merged = em.merge(jpa);
        return PromotionMapper.toDomain(merged);
    }
}