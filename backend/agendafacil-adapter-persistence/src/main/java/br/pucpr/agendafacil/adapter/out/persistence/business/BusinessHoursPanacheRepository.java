package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessHoursJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessHoursMapper;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import br.pucpr.agendafacil.domain.business.port.BusinessHoursRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso aos horários de funcionamento.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link BusinessHoursMapper}.</p>
 */
@ApplicationScoped
public class BusinessHoursPanacheRepository implements BusinessHoursRepository,
        PanacheRepository<BusinessHoursJpaEntity> {

    private final EntityManager em;

    @Inject
    public BusinessHoursPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public BusinessHours getById(Long id) {
        return findByIdOptional(id)
                .map(BusinessHoursMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<BusinessHours> findByBusinessId(Long businessId) {
        return list("business.id", businessId).stream()
                .map(BusinessHoursMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<BusinessHours> findByBusinessAndDayOfWeek(Long businessId, DayOfWeek day) {
        return find("business.id = ?1 and dayOfWeek = ?2", businessId, day)
                .firstResultOptional()
                .map(BusinessHoursMapper::toDomain);
    }

    @Override
    public void persist(BusinessHours hours) {
        BusinessHoursJpaEntity jpa = BusinessHoursMapper.toJpa(hours, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        hours.setId(jpa.getId());
    }

    @Override
    public BusinessHours update(BusinessHours hours) {
        BusinessHoursJpaEntity jpa = BusinessHoursMapper.toJpa(hours, em);
        BusinessHoursJpaEntity merged = em.merge(jpa);
        return BusinessHoursMapper.toDomain(merged);
    }

    @Override
    public void removeById(Long id) {
        delete("id", id);
    }
}