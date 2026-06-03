package br.pucpr.agendafacil.adapter.out.persistence.identity;

import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.jooq.Record;
import org.jooq.SelectOnConditionStep;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.CUSTOMERS;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.CUSTOMER_NOTIFICATION_PREFERENCES;
import static br.pucpr.agendafacil.adapter.out.persistence.jooq.generated.Tables.USERS;

@ApplicationScoped
public class CustomerJooqRepository implements CustomerRepository {

    private final DSLContext dsl;

    @Inject
    public CustomerJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Customer getById(Long id) {
        Record rec = baseSelect().where(USERS.ID.eq(id)).fetchOne();
        if (rec == null) return null;
        Customer c = toDomain(rec);
        c.setNotificationPreferences(loadPreferences(id));
        return c;
    }

    @Override
    public List<Customer> listAll(boolean activeOnly) {
        var query = activeOnly
                ? baseSelect().where(USERS.ACTIVE.isTrue())
                : baseSelect();
        return query.fetch().stream().map(rec -> {
            Customer c = toDomain(rec);
            c.setNotificationPreferences(loadPreferences(c.getId()));
            return c;
        }).toList();
    }

    @Override
    @Transactional
    public void persist(Customer customer) {
        if (customer.getId() != null) {
            update(customer);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Long id = dsl.insertInto(USERS)
                .set(USERS.NAME, customer.getName())
                .set(USERS.EMAIL, customer.getEmail())
                .set(USERS.PASSWORD, customer.getPassword())
                .set(USERS.PHONE, customer.getPhone())
                .set(USERS.ACTIVE, customer.isActive())
                .set(USERS.UPDATED_AT, now)
                .returning(USERS.ID)
                .fetchOne()
                .getId();

        dsl.insertInto(CUSTOMERS)
                .set(CUSTOMERS.ID, id)
                .set(CUSTOMERS.BIRTH_DATE, customer.getBirthDate())
                .execute();

        replacePreferences(id, customer.getNotificationPreferences());
        customer.setId(id);
    }

    @Override
    @Transactional
    public Customer update(Customer customer) {
        dsl.update(USERS)
                .set(USERS.NAME, customer.getName())
                .set(USERS.EMAIL, customer.getEmail())
                .set(USERS.PASSWORD, customer.getPassword())
                .set(USERS.PHONE, customer.getPhone())
                .set(USERS.ACTIVE, customer.isActive())
                .set(USERS.UPDATED_AT, LocalDateTime.now())
                .where(USERS.ID.eq(customer.getId()))
                .execute();

        dsl.update(CUSTOMERS)
                .set(CUSTOMERS.BIRTH_DATE, customer.getBirthDate())
                .where(CUSTOMERS.ID.eq(customer.getId()))
                .execute();

        replacePreferences(customer.getId(), customer.getNotificationPreferences());
        return getById(customer.getId());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SelectOnConditionStep<Record> baseSelect() {
        return (SelectOnConditionStep) dsl.select(
                USERS.ID, USERS.NAME, USERS.EMAIL, USERS.PASSWORD, USERS.PHONE, USERS.ACTIVE,
                CUSTOMERS.BIRTH_DATE)
                .from(USERS)
                .join(CUSTOMERS).on(CUSTOMERS.ID.eq(USERS.ID));
    }

    private Customer toDomain(Record rec) {
        Customer c = new Customer();
        c.setId(rec.get(USERS.ID));
        c.setName(rec.get(USERS.NAME));
        c.setEmail(rec.get(USERS.EMAIL));
        c.setPassword(rec.get(USERS.PASSWORD));
        c.setPhone(rec.get(USERS.PHONE));
        if (Boolean.TRUE.equals(rec.get(USERS.ACTIVE))) c.activate(); else c.deactivate();
        c.setBirthDate(rec.get(CUSTOMERS.BIRTH_DATE));
        return c;
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

    private void replacePreferences(Long customerId, Set<NotificationChannel> preferences) {
        dsl.deleteFrom(CUSTOMER_NOTIFICATION_PREFERENCES)
                .where(CUSTOMER_NOTIFICATION_PREFERENCES.CUSTOMER_ID.eq(customerId))
                .execute();
        if (preferences == null || preferences.isEmpty()) return;
        InsertValuesStep2<?, Long, String> insert = dsl.insertInto(CUSTOMER_NOTIFICATION_PREFERENCES)
                .columns(CUSTOMER_NOTIFICATION_PREFERENCES.CUSTOMER_ID,
                         CUSTOMER_NOTIFICATION_PREFERENCES.PREFERENCE);
        for (NotificationChannel ch : preferences) {
            insert = insert.values(customerId, ch.name());
        }
        insert.execute();
    }
}
