package br.pucpr.agendafacil.adapter.out.persistence.scheduling;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.APPOINTMENTS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.BUSINESSES;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.OFFERED_SERVICES;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.USERS;

@ApplicationScoped
public class AppointmentJooqRepository implements AppointmentRepository {

    private static final List<String> ACTIVE_STATUSES = List.of(
            AppointmentStatus.SCHEDULED.name(),
            AppointmentStatus.CONFIRMED.name());

    private final DSLContext dsl;

    @Inject
    public AppointmentJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Appointment getById(Long id) {
        Record rec = baseSelect().where(APPOINTMENTS.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public List<Appointment> findByBusinessAndDate(Long businessId, LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.atTime(23, 59, 59);
        return baseSelect()
                .where(APPOINTMENTS.BUSINESS_ID.eq(businessId))
                .and(APPOINTMENTS.SCHEDULED_AT.between(from, to))
                .fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByBusinessAndDateRange(Long businessId, LocalDate from, LocalDate to) {
        return baseSelect()
                .where(APPOINTMENTS.BUSINESS_ID.eq(businessId))
                .and(APPOINTMENTS.SCHEDULED_AT.between(from.atStartOfDay(), to.atTime(23, 59, 59)))
                .fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByCustomerId(Long customerId) {
        return baseSelect()
                .where(APPOINTMENTS.CUSTOMER_ID.eq(customerId))
                .orderBy(APPOINTMENTS.SCHEDULED_AT.desc())
                .fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findActiveByCustomerId(Long customerId) {
        return baseSelect()
                .where(APPOINTMENTS.CUSTOMER_ID.eq(customerId))
                .and(APPOINTMENTS.STATUS.in(ACTIVE_STATUSES))
                .orderBy(APPOINTMENTS.SCHEDULED_AT.asc())
                .fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Appointment> findByIdAndCustomerId(Long appointmentId, Long customerId) {
        Record rec = baseSelect()
                .where(APPOINTMENTS.ID.eq(appointmentId))
                .and(APPOINTMENTS.CUSTOMER_ID.eq(customerId))
                .fetchOne();
        return Optional.ofNullable(rec).map(this::toDomain);
    }

    @Override
    public Optional<Appointment> findByIdAndOwnerId(Long appointmentId, Long ownerId) {
        Record rec = baseSelect()
                .where(APPOINTMENTS.ID.eq(appointmentId))
                .and(BUSINESSES.OWNER_ID.eq(ownerId))
                .fetchOne();
        return Optional.ofNullable(rec).map(this::toDomain);
    }

    @Override
    public List<Appointment> findActiveAfter(LocalDateTime cutoff) {
        return baseSelect()
                .where(APPOINTMENTS.STATUS.in(ACTIVE_STATUSES))
                .and(APPOINTMENTS.SCHEDULED_AT.gt(cutoff))
                .fetch().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void persist(Appointment appointment) {
        if (appointment.getId() != null) {
            update(appointment);
            return;
        }
        Long id = dsl.insertInto(APPOINTMENTS)
                .set(APPOINTMENTS.CUSTOMER_ID, appointment.getCustomer().getId())
                .set(APPOINTMENTS.BUSINESS_ID, appointment.getBusiness().getId())
                .set(APPOINTMENTS.OFFERED_SERVICE_ID, appointment.getOfferedService().getId())
                .set(APPOINTMENTS.SCHEDULED_AT, appointment.getScheduledAt())
                .set(APPOINTMENTS.ESTIMATED_DURATION_MINUTES, appointment.getEstimatedDurationMinutes())
                .set(APPOINTMENTS.PRICE_PAID, appointment.getPricePaid())
                .set(APPOINTMENTS.STATUS, appointment.getStatus().name())
                .set(APPOINTMENTS.NOTES, appointment.getNotes())
                .set(APPOINTMENTS.CLINICAL_RECORD_REF, appointment.getClinicalRecordRef())
                .set(APPOINTMENTS.UPDATED_AT, LocalDateTime.now())
                .returning(APPOINTMENTS.ID)
                .fetchOne()
                .getId();
        appointment.setId(id);
    }

    @Override
    @Transactional
    public Appointment update(Appointment appointment) {
        dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.CUSTOMER_ID, appointment.getCustomer().getId())
                .set(APPOINTMENTS.BUSINESS_ID, appointment.getBusiness().getId())
                .set(APPOINTMENTS.OFFERED_SERVICE_ID, appointment.getOfferedService().getId())
                .set(APPOINTMENTS.SCHEDULED_AT, appointment.getScheduledAt())
                .set(APPOINTMENTS.ESTIMATED_DURATION_MINUTES, appointment.getEstimatedDurationMinutes())
                .set(APPOINTMENTS.PRICE_PAID, appointment.getPricePaid())
                .set(APPOINTMENTS.STATUS, appointment.getStatus().name())
                .set(APPOINTMENTS.NOTES, appointment.getNotes())
                .set(APPOINTMENTS.CLINICAL_RECORD_REF, appointment.getClinicalRecordRef())
                .set(APPOINTMENTS.UPDATED_AT, LocalDateTime.now())
                .where(APPOINTMENTS.ID.eq(appointment.getId()))
                .execute();
        return getById(appointment.getId());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SelectOnConditionStep<Record> baseSelect() {
        return (SelectOnConditionStep) dsl.select(
                APPOINTMENTS.ID,
                APPOINTMENTS.CUSTOMER_ID, APPOINTMENTS.BUSINESS_ID, APPOINTMENTS.OFFERED_SERVICE_ID,
                APPOINTMENTS.SCHEDULED_AT, APPOINTMENTS.ESTIMATED_DURATION_MINUTES,
                APPOINTMENTS.PRICE_PAID, APPOINTMENTS.STATUS,
                APPOINTMENTS.NOTES, APPOINTMENTS.CLINICAL_RECORD_REF,
                USERS.NAME.as("customer_name"),
                BUSINESSES.TRADE_NAME.as("business_trade_name"),
                BUSINESSES.OWNER_ID.as("business_owner_id"),
                OFFERED_SERVICES.NAME.as("offered_service_name"))
                .from(APPOINTMENTS)
                .leftJoin(USERS).on(USERS.ID.eq(APPOINTMENTS.CUSTOMER_ID))
                .leftJoin(BUSINESSES).on(BUSINESSES.ID.eq(APPOINTMENTS.BUSINESS_ID))
                .leftJoin(OFFERED_SERVICES).on(OFFERED_SERVICES.ID.eq(APPOINTMENTS.OFFERED_SERVICE_ID));
    }

    private Appointment toDomain(Record rec) {
        Appointment a = new Appointment();
        a.setId(rec.get(APPOINTMENTS.ID));

        Customer c = new Customer();
        c.setId(rec.get(APPOINTMENTS.CUSTOMER_ID));
        c.setName((String) rec.get("customer_name"));
        a.setCustomer(c);

        Business b = new Business();
        b.setId(rec.get(APPOINTMENTS.BUSINESS_ID));
        b.setTradeName((String) rec.get("business_trade_name"));
        a.setBusiness(b);

        OfferedService os = new OfferedService();
        os.setId(rec.get(APPOINTMENTS.OFFERED_SERVICE_ID));
        os.setName((String) rec.get("offered_service_name"));
        a.setOfferedService(os);

        a.setScheduledAt(rec.get(APPOINTMENTS.SCHEDULED_AT));
        a.setEstimatedDurationMinutes(rec.get(APPOINTMENTS.ESTIMATED_DURATION_MINUTES));
        a.setPricePaid(rec.get(APPOINTMENTS.PRICE_PAID));
        a.setStatus(AppointmentStatus.valueOf(rec.get(APPOINTMENTS.STATUS)));
        a.setNotes(rec.get(APPOINTMENTS.NOTES));
        a.setClinicalRecordRef(rec.get(APPOINTMENTS.CLINICAL_RECORD_REF));
        return a;
    }
}
