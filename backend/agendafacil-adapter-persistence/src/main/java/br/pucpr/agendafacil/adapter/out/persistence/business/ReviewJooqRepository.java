package br.pucpr.agendafacil.adapter.out.persistence.business;

import br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.tables.records.ReviewsRecord;
import br.pucpr.agendafacil.domain.business.Review;
import br.pucpr.agendafacil.domain.business.port.ReviewRepository;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Optional;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.APPOINTMENTS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.REVIEWS;

@ApplicationScoped
public class ReviewJooqRepository implements ReviewRepository {

    private final DSLContext dsl;

    @Inject
    public ReviewJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Review getById(Long id) {
        ReviewsRecord rec = dsl.selectFrom(REVIEWS).where(REVIEWS.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public Optional<Review> findByAppointmentId(Long appointmentId) {
        return dsl.selectFrom(REVIEWS)
                .where(REVIEWS.APPOINTMENT_ID.eq(appointmentId))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public List<Review> findByCustomerId(Long customerId) {
        return dsl.selectFrom(REVIEWS)
                .where(REVIEWS.CUSTOMER_ID.eq(customerId))
                .orderBy(REVIEWS.ID.desc())
                .fetch()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByBusinessId(Long businessId) {
        return dsl.select(REVIEWS.fields())
                .from(REVIEWS)
                .join(APPOINTMENTS).on(REVIEWS.APPOINTMENT_ID.eq(APPOINTMENTS.ID))
                .where(APPOINTMENTS.BUSINESS_ID.eq(businessId))
                .orderBy(REVIEWS.ID.desc())
                .fetchInto(REVIEWS)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void persist(Review review) {
        if (review.getId() != null) {
            update(review);
            return;
        }
        ReviewsRecord rec = dsl.newRecord(REVIEWS);
        rec.setAppointmentId(review.getAppointment().getId());
        rec.setCustomerId(review.getCustomer().getId());
        rec.setRating(review.getRating());
        rec.setComment(review.getComment());
        rec.store();
        review.setId(rec.getId());
    }

    @Override
    @Transactional
    public Review update(Review review) {
        dsl.update(REVIEWS)
                .set(REVIEWS.APPOINTMENT_ID, review.getAppointment().getId())
                .set(REVIEWS.CUSTOMER_ID, review.getCustomer().getId())
                .set(REVIEWS.RATING, review.getRating())
                .set(REVIEWS.COMMENT, review.getComment())
                .where(REVIEWS.ID.eq(review.getId()))
                .execute();
        return getById(review.getId());
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        dsl.deleteFrom(REVIEWS).where(REVIEWS.ID.eq(id)).execute();
    }

    private Review toDomain(ReviewsRecord rec) {
        Review r = new Review();
        r.setId(rec.getId());
        r.setRating(rec.getRating());
        r.setComment(rec.getComment());
        Appointment aStub = new Appointment();
        aStub.setId(rec.getAppointmentId());
        r.setAppointment(aStub);
        Customer cStub = new Customer();
        cStub.setId(rec.getCustomerId());
        r.setCustomer(cStub);
        return r;
    }
}
