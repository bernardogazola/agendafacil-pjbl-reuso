package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.ReviewJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.ReviewMapper;
import br.pucpr.agendafacil.domain.business.Review;
import br.pucpr.agendafacil.domain.business.port.ReviewRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso às avaliações.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link ReviewMapper}.</p>
 */
@ApplicationScoped
public class ReviewPanacheRepository implements ReviewRepository,
        PanacheRepository<ReviewJpaEntity> {

    private final EntityManager em;

    @Inject
    public ReviewPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Review getById(Long id) {
        return findByIdOptional(id)
                .map(ReviewMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Optional<Review> findByAppointmentId(Long appointmentId) {
        return find("appointment.id", appointmentId)
                .firstResultOptional()
                .map(ReviewMapper::toDomain);
    }

    @Override
    public List<Review> findByCustomerId(Long customerId) {
        return list("customer.id = ?1 order by id desc", customerId).stream()
                .map(ReviewMapper::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByBusinessId(Long businessId) {
        return list("appointment.business.id = ?1 order by id desc", businessId).stream()
                .map(ReviewMapper::toDomain)
                .toList();
    }

    @Override
    public void persist(Review review) {
        ReviewJpaEntity jpa = ReviewMapper.toJpa(review, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        review.setId(jpa.getId());
    }

    @Override
    public Review update(Review review) {
        ReviewJpaEntity jpa = ReviewMapper.toJpa(review, em);
        ReviewJpaEntity merged = em.merge(jpa);
        return ReviewMapper.toDomain(merged);
    }

    @Override
    public void removeById(Long id) {
        delete("id", id);
    }
}