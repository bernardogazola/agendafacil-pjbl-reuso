package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.identity.port.UserRepository;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.ADMINISTRATORS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.CUSTOMERS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.CUSTOMER_NOTIFICATION_PREFERENCES;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.USERS;

@ApplicationScoped
public class UserJooqRepository implements UserRepository {

    private final DSLContext dsl;

    @Inject
    public UserJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Record rec = dsl.select(
                USERS.ID, USERS.NAME, USERS.EMAIL, USERS.PASSWORD, USERS.PHONE, USERS.ACTIVE,
                CUSTOMERS.BIRTH_DATE,
                ADMINISTRATORS.ACCESS_LEVEL)
                .from(USERS)
                .leftJoin(CUSTOMERS).on(CUSTOMERS.ID.eq(USERS.ID))
                .leftJoin(ADMINISTRATORS).on(ADMINISTRATORS.ID.eq(USERS.ID))
                .where(USERS.EMAIL.eq(email))
                .fetchOne();
        if (rec == null) return Optional.empty();

        if (rec.get(ADMINISTRATORS.ACCESS_LEVEL) != null) {
            Administrator a = new Administrator();
            populateBase(a, rec);
            a.setAccessLevel(AccessLevel.valueOf(rec.get(ADMINISTRATORS.ACCESS_LEVEL)));
            return Optional.of(a);
        }
        // tudo o que não for admin (incluindo entradas com birth_date nulo) é cliente
        Customer c = new Customer();
        populateBase(c, rec);
        c.setBirthDate(rec.get(CUSTOMERS.BIRTH_DATE));
        c.setNotificationPreferences(loadPreferences(rec.get(USERS.ID)));
        return Optional.of(c);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(dsl.selectOne().from(USERS).where(USERS.EMAIL.eq(email)));
    }

    private void populateBase(User u, Record rec) {
        u.setId(rec.get(USERS.ID));
        u.setName(rec.get(USERS.NAME));
        u.setEmail(rec.get(USERS.EMAIL));
        u.setPassword(rec.get(USERS.PASSWORD));
        u.setPhone(rec.get(USERS.PHONE));
        if (Boolean.TRUE.equals(rec.get(USERS.ACTIVE))) u.activate(); else u.deactivate();
    }

    private Set<NotificationChannel> loadPreferences(Long customerId) {
        Set<NotificationChannel> out = EnumSet.noneOf(NotificationChannel.class);
        for (Record r : dsl.select(CUSTOMER_NOTIFICATION_PREFERENCES.PREFERENCE)
                .from(CUSTOMER_NOTIFICATION_PREFERENCES)
                .where(CUSTOMER_NOTIFICATION_PREFERENCES.CUSTOMER_ID.eq(customerId))
                .fetch()) {
            out.add(NotificationChannel.valueOf(r.get(CUSTOMER_NOTIFICATION_PREFERENCES.PREFERENCE)));
        }
        return out;
    }
}
