package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.OfferedServicesRecord;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.OFFERED_SERVICES;

@ApplicationScoped
public class OfferedServiceJooqRepository implements OfferedServiceRepository {

    private final DSLContext dsl;

    @Inject
    public OfferedServiceJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public OfferedService getById(Long id) {
        OfferedServicesRecord rec = dsl.selectFrom(OFFERED_SERVICES).where(OFFERED_SERVICES.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public Optional<OfferedService> findActiveById(Long id) {
        return dsl.selectFrom(OFFERED_SERVICES)
                .where(OFFERED_SERVICES.ID.eq(id))
                .and(OFFERED_SERVICES.ACTIVE.isTrue())
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public List<OfferedService> findByBusinessId(Long businessId) {
        return dsl.selectFrom(OFFERED_SERVICES)
                .where(OFFERED_SERVICES.BUSINESS_ID.eq(businessId))
                .fetch()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<OfferedService> findActiveByBusinessId(Long businessId) {
        return dsl.selectFrom(OFFERED_SERVICES)
                .where(OFFERED_SERVICES.BUSINESS_ID.eq(businessId))
                .and(OFFERED_SERVICES.ACTIVE.isTrue())
                .fetch()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void persist(OfferedService service) {
        if (service.getId() != null) {
            update(service);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        OfferedServicesRecord rec = dsl.newRecord(OFFERED_SERVICES);
        rec.setBusinessId(service.getBusiness().getId());
        rec.setName(service.getName());
        rec.setDescription(service.getDescription());
        rec.setBasePrice(service.getBasePrice());
        rec.setDurationMinutes(service.getDurationMinutes());
        rec.setCategory(service.getCategory());
        rec.setPricingPolicyType(service.getPricingPolicyType().name());
        rec.setActive(service.isActive());
        rec.setUpdatedAt(now);
        rec.store();
        service.setId(rec.getId());
    }

    @Override
    @Transactional
    public OfferedService update(OfferedService service) {
        dsl.update(OFFERED_SERVICES)
                .set(OFFERED_SERVICES.BUSINESS_ID, service.getBusiness().getId())
                .set(OFFERED_SERVICES.NAME, service.getName())
                .set(OFFERED_SERVICES.DESCRIPTION, service.getDescription())
                .set(OFFERED_SERVICES.BASE_PRICE, service.getBasePrice())
                .set(OFFERED_SERVICES.DURATION_MINUTES, service.getDurationMinutes())
                .set(OFFERED_SERVICES.CATEGORY, service.getCategory())
                .set(OFFERED_SERVICES.PRICING_POLICY_TYPE, service.getPricingPolicyType().name())
                .set(OFFERED_SERVICES.ACTIVE, service.isActive())
                .set(OFFERED_SERVICES.UPDATED_AT, LocalDateTime.now())
                .where(OFFERED_SERVICES.ID.eq(service.getId()))
                .execute();
        return getById(service.getId());
    }

    private OfferedService toDomain(OfferedServicesRecord rec) {
        OfferedService s = new OfferedService();
        s.setId(rec.getId());
        s.setName(rec.getName());
        s.setDescription(rec.getDescription());
        s.setBasePrice(rec.getBasePrice());
        s.setDurationMinutes(rec.getDurationMinutes());
        s.setCategory(rec.getCategory());
        s.setPricingPolicyType(PricingPolicyType.valueOf(rec.getPricingPolicyType()));
        if (Boolean.TRUE.equals(rec.getActive())) s.activate(); else s.deactivate();
        Business stub = new Business();
        stub.setId(rec.getBusinessId());
        s.setBusiness(stub);
        return s;
    }
}
