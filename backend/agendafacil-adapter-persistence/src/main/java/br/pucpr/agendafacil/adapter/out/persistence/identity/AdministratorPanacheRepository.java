package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.AdministratorJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.AdministratorMapper;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.port.AdministratorRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Repositório Panache responsável pelo acesso aos dados de {@link Administrator}.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. As conversões entre entidade JPA e domínio ficam
 * centralizadas em {@link AdministratorMapper}.</p>
 */
@ApplicationScoped
public class AdministratorPanacheRepository implements AdministratorRepository,
        PanacheRepository<AdministratorJpaEntity> {

    private final EntityManager em;

    @Inject
    public AdministratorPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Administrator getById(Long id) {
        return findByIdOptional(id)
                .map(AdministratorMapper::toDomain)
                .orElse(null);
    }

    @Override
    public void persist(Administrator administrator) {
        AdministratorJpaEntity jpa = AdministratorMapper.toJpa(administrator, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        administrator.setId(jpa.getId());
    }
}