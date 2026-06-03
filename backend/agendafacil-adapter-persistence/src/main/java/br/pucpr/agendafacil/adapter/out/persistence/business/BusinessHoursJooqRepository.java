package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.BusinessHoursRecord;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import br.pucpr.agendafacil.domain.business.port.BusinessHoursRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.BUSINESS_HOURS;

@ApplicationScoped
public class BusinessHoursJooqRepository implements BusinessHoursRepository {

    private final DSLContext dsl;

    @Inject
    public BusinessHoursJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public BusinessHours getById(Long id) {
        BusinessHoursRecord rec = dsl.selectFrom(BUSINESS_HOURS).where(BUSINESS_HOURS.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public List<BusinessHours> findByBusinessId(Long businessId) {
        return dsl.selectFrom(BUSINESS_HOURS)
                .where(BUSINESS_HOURS.BUSINESS_ID.eq(businessId))
                .fetch()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<BusinessHours> findByBusinessAndDayOfWeek(Long businessId, DayOfWeek day) {
        return dsl.selectFrom(BUSINESS_HOURS)
                .where(BUSINESS_HOURS.BUSINESS_ID.eq(businessId))
                .and(BUSINESS_HOURS.DAY_OF_WEEK.eq(day.name()))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void persist(BusinessHours hours) {
        if (hours.getId() != null) {
            update(hours);
            return;
        }
        BusinessHoursRecord rec = dsl.newRecord(BUSINESS_HOURS);
        rec.setBusinessId(hours.getBusiness().getId());
        rec.setDayOfWeek(hours.getDayOfWeek().name());
        rec.setStartTime(hours.getStartTime());
        rec.setEndTime(hours.getEndTime());
        rec.setActive(hours.isActive());
        rec.store();
        hours.setId(rec.getId());
    }

    @Override
    @Transactional
    public BusinessHours update(BusinessHours hours) {
        dsl.update(BUSINESS_HOURS)
                .set(BUSINESS_HOURS.BUSINESS_ID, hours.getBusiness().getId())
                .set(BUSINESS_HOURS.DAY_OF_WEEK, hours.getDayOfWeek().name())
                .set(BUSINESS_HOURS.START_TIME, hours.getStartTime())
                .set(BUSINESS_HOURS.END_TIME, hours.getEndTime())
                .set(BUSINESS_HOURS.ACTIVE, hours.isActive())
                .where(BUSINESS_HOURS.ID.eq(hours.getId()))
                .execute();
        return getById(hours.getId());
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        dsl.deleteFrom(BUSINESS_HOURS).where(BUSINESS_HOURS.ID.eq(id)).execute();
    }

    private BusinessHours toDomain(BusinessHoursRecord rec) {
        BusinessHours h = new BusinessHours();
        h.setId(rec.getId());
        h.setDayOfWeek(DayOfWeek.valueOf(rec.getDayOfWeek()));
        h.setStartTime(rec.getStartTime());
        h.setEndTime(rec.getEndTime());
        if (Boolean.TRUE.equals(rec.getActive())) h.activate(); else h.deactivate();
        Business stub = new Business();
        stub.setId(rec.getBusinessId());
        h.setBusiness(stub);
        return h;
    }
}
