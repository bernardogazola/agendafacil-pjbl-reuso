package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentJpaEntity;
import br.pucpr.agendafacil.domain.business.Review;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter avaliações entre o modelo de domínio e a
 * entidade JPA.
 *
 * <p>Na conversão para domínio, o agendamento e o cliente são preenchidos como
 * objetos parciais com id. Isso mantém a conversão simples e evita carregar
 * relacionamentos completos sem necessidade.</p>
 */
public final class ReviewMapper {

    private ReviewMapper() {}

    /**
     * Converte uma entidade JPA de avaliação para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return avaliação correspondente ou {@code null} quando a entrada for nula
     */
    public static Review toDomain(ReviewJpaEntity jpa) {
        if (jpa == null) return null;
        Review r = new Review();
        r.setId(jpa.getId());
        r.setRating(jpa.getRating());
        r.setComment(jpa.getComment());

        if (jpa.getAppointment() != null) {
            Appointment aStub = new Appointment();
            aStub.setId(jpa.getAppointment().getId());
            r.setAppointment(aStub);
        }
        if (jpa.getCustomer() != null) {
            Customer cStub = new Customer();
            cStub.setId(jpa.getCustomer().getId());
            r.setCustomer(cStub);
        }
        return r;
    }

    /**
     * Converte uma avaliação do domínio para a entidade JPA correspondente.
     *
     * <p>Quando a avaliação já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. As associações com agendamento e
     * cliente são feitas por referência.</p>
     *
     * @param r avaliação que será convertida
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static ReviewJpaEntity toJpa(Review r, EntityManager em) {
        if (r == null) return null;
        ReviewJpaEntity jpa = r.getId() != null
                ? em.find(ReviewJpaEntity.class, r.getId())
                : null;
        if (jpa == null) jpa = new ReviewJpaEntity();
        jpa.setRating(r.getRating());
        jpa.setComment(r.getComment());
        if (r.getAppointment() != null && r.getAppointment().getId() != null) {
            jpa.setAppointment(em.getReference(AppointmentJpaEntity.class, r.getAppointment().getId()));
        }
        if (r.getCustomer() != null && r.getCustomer().getId() != null) {
            jpa.setCustomer(em.getReference(CustomerJpaEntity.class, r.getCustomer().getId()));
        }
        return jpa;
    }
}