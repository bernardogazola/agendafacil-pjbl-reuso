package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.persistence.EntityManager;

import java.util.EnumSet;
import java.util.HashSet;

/**
 * Mapper responsável por converter clientes entre o modelo de domínio e a
 * entidade JPA.
 *
 * <p>Essa classe mantém separada a representação usada pelas regras de negócio
 * da representação usada pela persistência. Assim, o domínio não precisa
 * depender diretamente das entidades JPA.</p>
 *
 * <p>Os campos de auditoria não são preenchidos aqui, pois são controlados pela
 * própria entidade JPA base.</p>
 */
public final class CustomerMapper {

    private CustomerMapper() {}

    /**
     * Converte uma entidade JPA de cliente para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return cliente de domínio correspondente ou {@code null} quando a entrada for nula
     */
    public static Customer toDomain(CustomerJpaEntity jpa) {
        if (jpa == null) return null;
        Customer c = new Customer();
        c.setId(jpa.getId());
        c.setName(jpa.getName());
        c.setEmail(jpa.getEmail());
        c.setPassword(jpa.getPassword());
        c.setPhone(jpa.getPhone());
        if (jpa.isActive()) c.activate(); else c.deactivate();
        c.setBirthDate(jpa.getBirthDate());
        if (jpa.getNotificationPreferences() != null && !jpa.getNotificationPreferences().isEmpty()) {
            c.setNotificationPreferences(EnumSet.copyOf(jpa.getNotificationPreferences()));
        } else {
            c.setNotificationPreferences(EnumSet.noneOf(NotificationChannel.class));
        }
        return c;
    }

    /**
     * Converte um cliente de domínio para a entidade JPA correspondente.
     *
     * <p>Quando o cliente já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * @param c cliente de domínio que será convertido
     * @param em gerenciador de entidades usado para buscar registros existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static CustomerJpaEntity toJpa(Customer c, EntityManager em) {
        if (c == null) return null;
        CustomerJpaEntity jpa = c.getId() != null
                ? em.find(CustomerJpaEntity.class, c.getId())
                : null;
        if (jpa == null) jpa = new CustomerJpaEntity();
        jpa.setName(c.getName());
        jpa.setEmail(c.getEmail());
        jpa.setPassword(c.getPassword());
        jpa.setPhone(c.getPhone());
        jpa.setActive(c.isActive());
        jpa.setBirthDate(c.getBirthDate());
        jpa.setNotificationPreferences(
                c.getNotificationPreferences() == null
                        ? new HashSet<>()
                        : new HashSet<>(c.getNotificationPreferences())
        );
        return jpa;
    }
}