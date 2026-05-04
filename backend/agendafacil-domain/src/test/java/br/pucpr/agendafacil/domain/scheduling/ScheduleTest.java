package br.pucpr.agendafacil.domain.scheduling;

import br.pucpr.agendafacil.domain.business.*;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.availability.FixedHoursAvailability;
import br.pucpr.agendafacil.domain.scheduling.cache.AppointmentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 4, 13);

    @BeforeEach
    void resetRegistry() {
        AppointmentRegistry.getInstance().clear();
    }

    @Test
    void availableSlots_excludesTakenSlots() {
        Business business = businessOpen(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        Appointment taken = appointmentAt(business, LocalDateTime.of(MONDAY, LocalTime.of(10, 0)));
        Schedule schedule = new Schedule(business, MONDAY, MONDAY, List.of(taken));

        List<LocalTime> slots = schedule.availableSlots(MONDAY, new FixedHoursAvailability());

        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(11, 0)), slots);
    }

    @Test
    void hasConflict_detectsOverlapInRegistry() {
        Business business = businessOpen(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        Appointment cached = appointmentAt(business, LocalDateTime.of(MONDAY, LocalTime.of(10, 0)));
        AppointmentRegistry.getInstance().register(cached);
        Schedule schedule = new Schedule(business, MONDAY, MONDAY, List.of());

        boolean conflict = schedule.hasConflict(
                LocalDateTime.of(MONDAY, LocalTime.of(10, 15)), 30);

        assertTrue(conflict, "Deve detectar overlap contra o Registry");
    }

    @Test
    void hasConflict_detectsOverlapInLocalAppointments() {
        Business business = businessOpen(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        Appointment local = appointmentAt(business, LocalDateTime.of(MONDAY, LocalTime.of(10, 0)));
        Schedule schedule = new Schedule(business, MONDAY, MONDAY, List.of(local));

        assertTrue(schedule.hasConflict(LocalDateTime.of(MONDAY, LocalTime.of(10, 15)), 30));
    }

    @Test
    void hasConflict_returnsFalseWhenWindowClear() {
        Business business = businessOpen(1L, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        Appointment elsewhere = appointmentAt(business, LocalDateTime.of(MONDAY, LocalTime.of(9, 0)));
        AppointmentRegistry.getInstance().register(elsewhere);
        Schedule schedule = new Schedule(business, MONDAY, MONDAY, List.of());

        assertFalse(schedule.hasConflict(LocalDateTime.of(MONDAY, LocalTime.of(11, 0)), 30));
    }

    private static Business businessOpen(long id, DayOfWeek day, LocalTime start, LocalTime end) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        setId(business, id);
        business.getBusinessHours().add(new BusinessHours(business, day, start, end));
        return business;
    }

    private static Appointment appointmentAt(Business business, LocalDateTime at) {
        Customer customer = new Customer();
        OfferedService service = new OfferedService("Serviço",
                new BigDecimal("50.00"), 30, business);
        return new Appointment(customer, business, service, at, 60,
                new BigDecimal("50.00"));
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
