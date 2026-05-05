package br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.BusinessJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.business.jpa.OfferedServiceJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.CustomerJpaEntity;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter agendamentos entre o modelo de domínio e a
 * entidade JPA.
 *
 * <p>Essa classe isola a conversão entre a camada de domínio e a camada de
 * persistência, evitando que as regras de negócio dependam diretamente das
 * entidades JPA.</p>
 *
 * <p>Na conversão para domínio, cliente, estabelecimento e serviço são
 * preenchidos como objetos parciais com id. Isso evita carregar o grafo completo
 * quando o caso de uso precisa apenas das referências principais do
 * agendamento.</p>
 */
public final class AppointmentMapper {

    private AppointmentMapper() {}

    /**
     * Converte uma entidade JPA de agendamento para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return agendamento correspondente ou {@code null} quando a entrada for nula
     */
    public static Appointment toDomain(AppointmentJpaEntity jpa) {
        if (jpa == null) return null;
        Appointment a = new Appointment();
        a.setId(jpa.getId());

        if (jpa.getCustomer() != null) {
            Customer cStub = new Customer();
            cStub.setId(jpa.getCustomer().getId());
            a.setCustomer(cStub);
        }
        if (jpa.getBusiness() != null) {
            Business bStub = new Business();
            bStub.setId(jpa.getBusiness().getId());
            a.setBusiness(bStub);
        }
        if (jpa.getOfferedService() != null) {
            OfferedService osStub = new OfferedService();
            osStub.setId(jpa.getOfferedService().getId());
            a.setOfferedService(osStub);
        }

        a.setScheduledAt(jpa.getScheduledAt());
        a.setEstimatedDurationMinutes(jpa.getEstimatedDurationMinutes());
        a.setPricePaid(jpa.getPricePaid());
        a.setStatus(jpa.getStatus());
        a.setNotes(jpa.getNotes());
        a.setClinicalRecordRef(jpa.getClinicalRecordRef());
        return a;
    }

    /**
     * Converte um agendamento do domínio para a entidade JPA correspondente.
     *
     * <p>Quando o agendamento já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * <p>As associações com cliente, estabelecimento e serviço são feitas por
     * referência, usando os ids presentes no objeto de domínio.</p>
     *
     * @param a agendamento que será convertido
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static AppointmentJpaEntity toJpa(Appointment a, EntityManager em) {
        if (a == null) return null;
        AppointmentJpaEntity jpa = a.getId() != null
                ? em.find(AppointmentJpaEntity.class, a.getId())
                : null;
        if (jpa == null) jpa = new AppointmentJpaEntity();

        jpa.setCustomer(em.getReference(CustomerJpaEntity.class, a.getCustomer().getId()));
        jpa.setBusiness(em.getReference(BusinessJpaEntity.class, a.getBusiness().getId()));
        jpa.setOfferedService(em.getReference(OfferedServiceJpaEntity.class,
                a.getOfferedService().getId()));

        jpa.setScheduledAt(a.getScheduledAt());
        jpa.setEstimatedDurationMinutes(a.getEstimatedDurationMinutes());
        jpa.setPricePaid(a.getPricePaid());
        jpa.setStatus(a.getStatus());
        jpa.setNotes(a.getNotes());
        jpa.setClinicalRecordRef(a.getClinicalRecordRef());
        return jpa;
    }
}