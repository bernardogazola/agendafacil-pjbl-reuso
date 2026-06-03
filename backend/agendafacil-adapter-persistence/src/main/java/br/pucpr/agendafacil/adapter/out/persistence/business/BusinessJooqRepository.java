package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.BusinessesRecord;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.BUSINESSES;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.USERS;

@ApplicationScoped
public class BusinessJooqRepository implements BusinessRepository {

    private final DSLContext dsl;

    @Inject
    public BusinessJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Business getById(Long id) {
        Record rec = baseSelect().where(BUSINESSES.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public Optional<Business> findActiveById(Long id) {
        Record rec = baseSelect().where(BUSINESSES.ID.eq(id)).and(BUSINESSES.ACTIVE.isTrue()).fetchOne();
        return Optional.ofNullable(rec).map(this::toDomain);
    }

    @Override
    public List<Business> findByOwnerId(Long ownerId) {
        return baseSelect().where(BUSINESSES.OWNER_ID.eq(ownerId)).fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Business> listActive() {
        return baseSelect().where(BUSINESSES.ACTIVE.isTrue()).fetch().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Business> listAllAdmin(boolean activeOnly) {
        var query = baseSelect();
        if (activeOnly) {
            return query.where(BUSINESSES.ACTIVE.isTrue()).fetch().stream().map(this::toDomain).toList();
        }
        return query.fetch().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void persist(Business business) {
        if (business.getId() != null) {
            update(business);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        BusinessesRecord rec = dsl.newRecord(BUSINESSES);
        rec.setTradeName(business.getTradeName());
        rec.setTaxId(business.getTaxId());
        rec.setEmail(business.getEmail());
        rec.setPhone(business.getPhone());
        rec.setCategory(business.getCategory().name());
        rec.setPlan(business.getPlan().name());
        rec.setCancellationPolicyType(business.getCancellationPolicyType().name());
        rec.setActive(business.isActive());
        rec.setOwnerId(business.getOwner().getId());
        rec.setUpdatedAt(now);
        rec.store();
        business.setId(rec.getId());
    }

    @Override
    @Transactional
    public Business update(Business business) {
        dsl.update(BUSINESSES)
                .set(BUSINESSES.TRADE_NAME, business.getTradeName())
                .set(BUSINESSES.TAX_ID, business.getTaxId())
                .set(BUSINESSES.EMAIL, business.getEmail())
                .set(BUSINESSES.PHONE, business.getPhone())
                .set(BUSINESSES.CATEGORY, business.getCategory().name())
                .set(BUSINESSES.PLAN, business.getPlan().name())
                .set(BUSINESSES.CANCELLATION_POLICY_TYPE, business.getCancellationPolicyType().name())
                .set(BUSINESSES.ACTIVE, business.isActive())
                .set(BUSINESSES.OWNER_ID, business.getOwner().getId())
                .set(BUSINESSES.UPDATED_AT, LocalDateTime.now())
                .where(BUSINESSES.ID.eq(business.getId()))
                .execute();
        return getById(business.getId());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private org.jooq.SelectOnConditionStep<Record> baseSelect() {
        return (org.jooq.SelectOnConditionStep) dsl.select(
                BUSINESSES.ID, BUSINESSES.TRADE_NAME, BUSINESSES.TAX_ID,
                BUSINESSES.EMAIL, BUSINESSES.PHONE,
                BUSINESSES.CATEGORY, BUSINESSES.PLAN, BUSINESSES.CANCELLATION_POLICY_TYPE,
                BUSINESSES.ACTIVE, BUSINESSES.OWNER_ID,
                USERS.NAME.as("owner_name"), USERS.EMAIL.as("owner_email"))
                .from(BUSINESSES)
                .leftJoin(USERS).on(BUSINESSES.OWNER_ID.eq(USERS.ID));
    }

    private Business toDomain(Record rec) {
        Business b = new Business();
        b.setId(rec.get(BUSINESSES.ID));
        b.setTradeName(rec.get(BUSINESSES.TRADE_NAME));
        b.setTaxId(rec.get(BUSINESSES.TAX_ID));
        b.setEmail(rec.get(BUSINESSES.EMAIL));
        b.setPhone(rec.get(BUSINESSES.PHONE));
        b.setCategory(BusinessCategory.valueOf(rec.get(BUSINESSES.CATEGORY)));
        b.setPlan(BusinessPlan.valueOf(rec.get(BUSINESSES.PLAN)));
        b.setCancellationPolicyType(CancellationPolicyType.valueOf(rec.get(BUSINESSES.CANCELLATION_POLICY_TYPE)));
        if (Boolean.TRUE.equals(rec.get(BUSINESSES.ACTIVE))) b.activate(); else b.deactivate();

        Administrator owner = new Administrator();
        owner.setId(rec.get(BUSINESSES.OWNER_ID));
        owner.setName((String) rec.get("owner_name"));
        owner.setEmail((String) rec.get("owner_email"));
        b.setOwner(owner);
        return b;
    }
}
