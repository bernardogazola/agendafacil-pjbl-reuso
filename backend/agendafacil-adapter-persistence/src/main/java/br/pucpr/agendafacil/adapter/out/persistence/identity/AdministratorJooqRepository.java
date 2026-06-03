package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.port.AdministratorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.ADMINISTRATORS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.USERS;

@ApplicationScoped
public class AdministratorJooqRepository implements AdministratorRepository {

    private final DSLContext dsl;

    @Inject
    public AdministratorJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Administrator getById(Long id) {
        Record rec = baseSelect().where(USERS.ID.eq(id)).fetchOne();
        return rec == null ? null : toDomain(rec);
    }

    @Override
    public List<Administrator> listAll(boolean activeOnly) {
        var query = activeOnly
                ? baseSelect().where(USERS.ACTIVE.isTrue())
                : baseSelect();
        return query.fetch().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void persist(Administrator administrator) {
        if (administrator.getId() != null) {
            update(administrator);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Long id = dsl.insertInto(USERS)
                .set(USERS.NAME, administrator.getName())
                .set(USERS.EMAIL, administrator.getEmail())
                .set(USERS.PASSWORD, administrator.getPassword())
                .set(USERS.PHONE, administrator.getPhone())
                .set(USERS.ACTIVE, administrator.isActive())
                .set(USERS.UPDATED_AT, now)
                .returning(USERS.ID)
                .fetchOne()
                .getId();

        dsl.insertInto(ADMINISTRATORS)
                .set(ADMINISTRATORS.ID, id)
                .set(ADMINISTRATORS.ACCESS_LEVEL, administrator.getAccessLevel().name())
                .execute();

        administrator.setId(id);
    }

    @Override
    @Transactional
    public Administrator update(Administrator administrator) {
        dsl.update(USERS)
                .set(USERS.NAME, administrator.getName())
                .set(USERS.EMAIL, administrator.getEmail())
                .set(USERS.PASSWORD, administrator.getPassword())
                .set(USERS.PHONE, administrator.getPhone())
                .set(USERS.ACTIVE, administrator.isActive())
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(administrator.getId()))
                .execute();

        dsl.update(ADMINISTRATORS)
                .set(ADMINISTRATORS.ACCESS_LEVEL, administrator.getAccessLevel().name())
                .where(ADMINISTRATORS.ID.eq(administrator.getId()))
                .execute();

        return getById(administrator.getId());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private org.jooq.SelectOnConditionStep<Record> baseSelect() {
        return (org.jooq.SelectOnConditionStep) dsl.select(
                USERS.ID, USERS.NAME, USERS.EMAIL, USERS.PASSWORD, USERS.PHONE, USERS.ACTIVE,
                ADMINISTRATORS.ACCESS_LEVEL)
                .from(USERS)
                .join(ADMINISTRATORS).on(ADMINISTRATORS.ID.eq(USERS.ID));
    }

    private Administrator toDomain(Record rec) {
        Administrator a = new Administrator();
        a.setId(rec.get(USERS.ID));
        a.setName(rec.get(USERS.NAME));
        a.setEmail(rec.get(USERS.EMAIL));
        a.setPassword(rec.get(USERS.PASSWORD));
        a.setPhone(rec.get(USERS.PHONE));
        if (Boolean.TRUE.equals(rec.get(USERS.ACTIVE))) a.activate(); else a.deactivate();
        a.setAccessLevel(AccessLevel.valueOf(rec.get(ADMINISTRATORS.ACCESS_LEVEL)));
        return a;
    }
}
