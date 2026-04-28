package br.pucpr.agendafacil.domain.scheduling.cache;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentRegistryTest {

    @BeforeEach
    void resetSingleton() {
        AppointmentRegistry.reset();
    }

    @Test
    void getInstance_returnsSameInstance() {
        assertSame(AppointmentRegistry.getInstance(), AppointmentRegistry.getInstance());
    }

    @Test
    void register_thenForDay_returnsIt() {
        Business business = businessWithId(1L);
        Appointment appointment = appointmentOn(business, LocalDateTime.of(2026, 4, 17, 10, 0));

        AppointmentRegistry.getInstance().register(appointment);

        List<Appointment> day = AppointmentRegistry.getInstance()
                .forDay(LocalDate.of(2026, 4, 17), 1L);
        assertEquals(1, day.size());
        assertSame(appointment, day.getFirst());
    }

    @Test
    void forDay_filtersByBusinessAndDate() {
        Business b1 = businessWithId(1L);
        Business b2 = businessWithId(2L);
        AppointmentRegistry registry = AppointmentRegistry.getInstance();

        Appointment a1 = appointmentOn(b1, LocalDateTime.of(2026, 4, 17, 10, 0));
        Appointment a2 = appointmentOn(b2, LocalDateTime.of(2026, 4, 17, 10, 0));
        Appointment a3 = appointmentOn(b1, LocalDateTime.of(2026, 4, 18, 10, 0));
        registry.register(a1);
        registry.register(a2);
        registry.register(a3);

        List<Appointment> day = registry.forDay(LocalDate.of(2026, 4, 17), 1L);
        assertEquals(1, day.size());
        assertSame(a1, day.getFirst());
    }

    @Test
    void unregister_removes() {
        Business business = businessWithId(1L);
        Appointment appointment = appointmentOn(business, LocalDateTime.of(2026, 4, 17, 10, 0));
        AppointmentRegistry registry = AppointmentRegistry.getInstance();
        registry.register(appointment);

        registry.unregister(appointment);

        assertTrue(registry.forDay(LocalDate.of(2026, 4, 17), 1L).isEmpty());
    }

    @Test
    void clear_emptiesCache() {
        Business business = businessWithId(1L);
        AppointmentRegistry registry = AppointmentRegistry.getInstance();
        registry.register(appointmentOn(business, LocalDateTime.of(2026, 4, 17, 10, 0)));
        registry.register(appointmentOn(business, LocalDateTime.of(2026, 4, 17, 11, 0)));

        registry.clear();

        assertTrue(registry.forDay(LocalDate.of(2026, 4, 17), 1L).isEmpty());
    }

    private static Business businessWithId(long id) {
        Business business = new Business("Negócio", "teste" + id + "@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        setId(business, id);
        return business;
    }

    private static Appointment appointmentOn(Business business, LocalDateTime at) {
        Customer customer = new Customer();
        OfferedService service = new OfferedService("Serviço",
                new BigDecimal("50.00"), 30, business);
        return new Appointment(customer, business, service, at, 30, new BigDecimal("50.00"));
    }

    private static void setId(Object entity, long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}