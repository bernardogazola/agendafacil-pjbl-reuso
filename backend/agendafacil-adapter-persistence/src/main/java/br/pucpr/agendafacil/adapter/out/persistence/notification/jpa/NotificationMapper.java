package br.pucpr.agendafacil.adapter.out.persistence.notification.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.AdministratorJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.UserJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentJpaEntity;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.notification.Notification;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter notificações entre o modelo de domínio e a
 * entidade JPA.
 *
 * <p>Essa classe isola a conversão entre a camada de domínio e a camada de
 * persistência, evitando que as regras de negócio dependam diretamente das
 * entidades JPA.</p>
 *
 * <p>Na conversão para domínio, o agendamento e o destinatário são preenchidos
 * como objetos parciais com id. Isso evita carregar o grafo completo quando o
 * caso de uso precisa apenas das referências principais da notificação.</p>
 */
public final class NotificationMapper {

    private NotificationMapper() {}

    /**
     * Converte uma entidade JPA de notificação para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return notificação correspondente ou {@code null} quando a entrada for nula
     */
    public static Notification toDomain(NotificationJpaEntity jpa) {
        if (jpa == null) return null;
        Notification n = new Notification();
        n.setId(jpa.getId());
        n.setType(jpa.getType());
        n.setChannel(jpa.getChannel());
        n.setMessage(jpa.getMessage());
        n.setStatus(jpa.getStatus());
        n.setSentAt(jpa.getSentAt());

        if (jpa.getAppointment() != null) {
            Appointment aStub = new Appointment();
            aStub.setId(jpa.getAppointment().getId());
            n.setAppointment(aStub);
        }
        if (jpa.getRecipient() != null) {
            n.setRecipient(stubRecipient(jpa.getRecipient()));
        }
        return n;
    }

    /**
     * Converte uma notificação do domínio para a entidade JPA correspondente.
     *
     * <p>Quando a notificação já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * <p>As associações com agendamento e destinatário são feitas por referência,
     * usando os ids presentes no objeto de domínio.</p>
     *
     * @param n notificação que será convertida
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static NotificationJpaEntity toJpa(Notification n, EntityManager em) {
        if (n == null) return null;
        NotificationJpaEntity jpa = n.getId() != null
                ? em.find(NotificationJpaEntity.class, n.getId())
                : null;
        if (jpa == null) jpa = new NotificationJpaEntity();
        jpa.setType(n.getType());
        jpa.setChannel(n.getChannel());
        jpa.setMessage(n.getMessage());
        jpa.setStatus(n.getStatus());
        jpa.setSentAt(n.getSentAt());

        if (n.getAppointment() != null && n.getAppointment().getId() != null) {
            jpa.setAppointment(em.getReference(AppointmentJpaEntity.class, n.getAppointment().getId()));
        }
        if (n.getRecipient() != null && n.getRecipient().getId() != null) {
            Class<? extends UserJpaEntity> targetClass = n.getRecipient() instanceof Administrator
                    ? AdministratorJpaEntity.class
                    : CustomerJpaEntity.class;
            jpa.setRecipient(em.getReference(targetClass, n.getRecipient().getId()));
        }
        return jpa;
    }

    /**
     * Cria um usuário parcial com id a partir do subtipo JPA do destinatário.
     *
     * @param jpa entidade JPA do destinatário
     * @return usuário parcial correspondente ao subtipo encontrado
     * @throws IllegalStateException se o subtipo da entidade JPA não for reconhecido
     */
    private static User stubRecipient(UserJpaEntity jpa) {
        return switch (jpa) {
            case CustomerJpaEntity ignored -> {
                Customer c = new Customer();
                c.setId(jpa.getId());
                yield c;
            }
            case AdministratorJpaEntity ignored -> {
                Administrator a = new Administrator();
                a.setId(jpa.getId());
                yield a;
            }
            default -> throw new IllegalStateException(
                    "Subtipo de UserJpaEntity desconhecido em Notification.recipient: "
                            + jpa.getClass().getName());
        };
    }
}