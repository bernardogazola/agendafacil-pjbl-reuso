package br.pucpr.agendafacil.adapter.out.persistence.notification;

import br.pucpr.agendafacil.adapter.out.persistence.notification.jpa.NotificationJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.notification.jpa.NotificationMapper;
import br.pucpr.agendafacil.domain.notification.Notification;
import br.pucpr.agendafacil.domain.notification.port.NotificationRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Repositório Panache responsável pelo acesso aos registros de notificação.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. Internamente ele trabalha com
 * {@link NotificationJpaEntity}, mas recebe objetos do domínio e converte por
 * meio de {@link NotificationMapper}.</p>
 */
@ApplicationScoped
public class NotificationPanacheRepository implements NotificationRepository,
        PanacheRepository<NotificationJpaEntity> {

    private final EntityManager em;

    @Inject
    public NotificationPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void persist(Notification notification) {
        NotificationJpaEntity jpa = NotificationMapper.toJpa(notification, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        notification.setId(jpa.getId());
    }
}