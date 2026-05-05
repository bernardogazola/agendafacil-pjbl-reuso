package br.pucpr.agendafacil.adapter.out.persistence.scheduling;

import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentJpaEntity;
import br.pucpr.agendafacil.adapter.out.persistence.scheduling.jpa.AppointmentMapper;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Panache responsável pelo acesso aos agendamentos.
 *
 * <p>Este repositório adapta a persistência JPA/Panache para a porta de saída
 * usada pela aplicação. Internamente ele trabalha com
 * {@link AppointmentJpaEntity}, mas devolve objetos do domínio por meio de
 * {@link AppointmentMapper}.</p>
 *
 * <p>As consultas por data convertem {@link LocalDate} para intervalos de
 * {@link LocalDateTime}, permitindo buscar os agendamentos dentro de um dia ou
 * de um período completo.</p>
 */
@ApplicationScoped
public class AppointmentPanacheRepository implements AppointmentRepository,
        PanacheRepository<AppointmentJpaEntity> {

    private final EntityManager em;

    @Inject
    public AppointmentPanacheRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Appointment getById(Long id) {
        return findByIdOptional(id)
                .map(AppointmentMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Appointment> findByBusinessAndDate(Long businessId, LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.atTime(23, 59, 59);
        return list("business.id = ?1 and scheduledAt between ?2 and ?3",
                businessId, from, to)
                .stream()
                .map(AppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByBusinessAndDateRange(Long businessId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);
        return list("business.id = ?1 and scheduledAt between ?2 and ?3",
                businessId, start, end)
                .stream()
                .map(AppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByCustomerId(Long customerId) {
        return list("customer.id = ?1 order by scheduledAt desc", customerId)
                .stream()
                .map(AppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findActiveByCustomerId(Long customerId) {
        return list("customer.id = ?1 and status in ?2 order by scheduledAt",
                customerId,
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED))
                .stream()
                .map(AppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Appointment> findByIdAndCustomerId(Long appointmentId, Long customerId) {
        return find("id = ?1 and customer.id = ?2", appointmentId, customerId)
                .firstResultOptional()
                .map(AppointmentMapper::toDomain);
    }

    @Override
    public List<Appointment> findActiveAfter(LocalDateTime cutoff) {
        return list("status in ?1 and scheduledAt > ?2",
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED),
                cutoff)
                .stream()
                .map(AppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public void persist(Appointment appointment) {
        AppointmentJpaEntity jpa = AppointmentMapper.toJpa(appointment, em);
        if (jpa.getId() == null) {
            persist(jpa);
        }
        appointment.setId(jpa.getId());
    }
}