package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.PromotionsRecord;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.Promotion;
import br.pucpr.agendafacil.domain.business.port.PromotionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.PROMOTIONS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.PROMOTION_OFFERED_SERVICES;

@ApplicationScoped
public class PromotionJooqRepository implements PromotionRepository {

    private final DSLContext dsl;

    @Inject
    public PromotionJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Promotion getById(Long id) {
        PromotionsRecord rec = dsl.selectFrom(PROMOTIONS).where(PROMOTIONS.ID.eq(id)).fetchOne();
        if (rec == null) return null;
        Promotion p = toDomain(rec);
        p.setEligibleServices(loadEligibleServices(id));
        return p;
    }

    @Override
    public List<Promotion> findByBusinessId(Long businessId) {
        List<PromotionsRecord> rows = dsl.selectFrom(PROMOTIONS)
                .where(PROMOTIONS.BUSINESS_ID.eq(businessId))
                .orderBy(PROMOTIONS.VALID_FROM.desc())
                .fetch();
        return rows.stream().map(rec -> {
            Promotion p = toDomain(rec);
            p.setEligibleServices(loadEligibleServices(rec.getId()));
            return p;
        }).toList();
    }

    @Override
    @Transactional
    public void persist(Promotion promotion) {
        if (promotion.getId() != null) {
            update(promotion);
            return;
        }
        PromotionsRecord rec = dsl.newRecord(PROMOTIONS);
        rec.setBusinessId(promotion.getBusiness().getId());
        rec.setName(promotion.getName());
        rec.setDescription(promotion.getDescription());
        rec.setDiscountPercentage(promotion.getDiscountPercentage());
        rec.setDiscountAmount(promotion.getDiscountAmount());
        rec.setValidFrom(promotion.getValidFrom());
        rec.setValidTo(promotion.getValidTo());
        rec.setActive(promotion.isActive());
        rec.store();
        promotion.setId(rec.getId());
        replaceEligibleServices(rec.getId(), promotion.getEligibleServices());
    }

    @Override
    @Transactional
    public Promotion update(Promotion promotion) {
        dsl.update(PROMOTIONS)
                .set(PROMOTIONS.BUSINESS_ID, promotion.getBusiness().getId())
                .set(PROMOTIONS.NAME, promotion.getName())
                .set(PROMOTIONS.DESCRIPTION, promotion.getDescription())
                .set(PROMOTIONS.DISCOUNT_PERCENTAGE, promotion.getDiscountPercentage())
                .set(PROMOTIONS.DISCOUNT_AMOUNT, promotion.getDiscountAmount())
                .set(PROMOTIONS.VALID_FROM, promotion.getValidFrom())
                .set(PROMOTIONS.VALID_TO, promotion.getValidTo())
                .set(PROMOTIONS.ACTIVE, promotion.isActive())
                .where(PROMOTIONS.ID.eq(promotion.getId()))
                .execute();
        replaceEligibleServices(promotion.getId(), promotion.getEligibleServices());
        return getById(promotion.getId());
    }

    private void replaceEligibleServices(Long promotionId, Set<OfferedService> eligibles) {
        dsl.deleteFrom(PROMOTION_OFFERED_SERVICES)
                .where(PROMOTION_OFFERED_SERVICES.PROMOTION_ID.eq(promotionId))
                .execute();
        if (eligibles == null || eligibles.isEmpty()) return;
        var insert = dsl.insertInto(PROMOTION_OFFERED_SERVICES)
                .columns(PROMOTION_OFFERED_SERVICES.PROMOTION_ID, PROMOTION_OFFERED_SERVICES.OFFERED_SERVICE_ID);
        for (OfferedService os : eligibles) {
            if (os.getId() != null) insert.values(promotionId, os.getId());
        }
        insert.execute();
    }

    private Set<OfferedService> loadEligibleServices(Long promotionId) {
        Set<OfferedService> result = new HashSet<>();
        for (Record r : dsl.select(PROMOTION_OFFERED_SERVICES.OFFERED_SERVICE_ID)
                .from(PROMOTION_OFFERED_SERVICES)
                .where(PROMOTION_OFFERED_SERVICES.PROMOTION_ID.eq(promotionId))
                .fetch()) {
            OfferedService stub = new OfferedService();
            stub.setId(r.get(PROMOTION_OFFERED_SERVICES.OFFERED_SERVICE_ID));
            result.add(stub);
        }
        return result;
    }

    private Promotion toDomain(PromotionsRecord rec) {
        Promotion p = new Promotion();
        p.setId(rec.getId());
        p.setName(rec.getName());
        p.setDescription(rec.getDescription());
        p.setDiscountPercentage(rec.getDiscountPercentage());
        p.setDiscountAmount(rec.getDiscountAmount());
        p.setValidFrom(rec.getValidFrom());
        p.setValidTo(rec.getValidTo());
        if (Boolean.TRUE.equals(rec.getActive())) p.activate(); else p.deactivate();
        Business stub = new Business();
        stub.setId(rec.getBusinessId());
        p.setBusiness(stub);
        return p;
    }
}
