package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityStrategyTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 4, 13);

    @Test
    void fixedHours_returnsHourlySlotsBetweenStartAndEnd() {
        Business business = businessOpen(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        List<LocalTime> slots = new FixedHoursAvailability().availableSlots(business, MONDAY);

        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)),
                slots);
    }

    @Test
    void slotBased_respectsGranularity() {
        Business business = businessOpen(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        List<LocalTime> slots = new SlotBasedAvailability(30).availableSlots(business, MONDAY);

        assertEquals(List.of(
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30)
        ), slots);
    }

    @Test
    void onDemand_returnsEmptyList() {
        Business business = businessOpen(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertTrue(new OnDemandAvailability().availableSlots(business, MONDAY).isEmpty());
    }

    @Test
    void fixedHours_returnsEmptyWhenDayNotConfigured() {
        Business business = businessOpen(DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertTrue(new FixedHoursAvailability().availableSlots(business, MONDAY).isEmpty());
    }

    @Test
    void fixedHours_returnsEmptyWhenDayInactive() {
        Business business = businessOpen(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        business.getBusinessHours().getFirst().deactivate();

        assertTrue(new FixedHoursAvailability().availableSlots(business, MONDAY).isEmpty());
    }

    private static Business businessOpen(DayOfWeek day, LocalTime start, LocalTime end) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        BusinessHours hours = new BusinessHours(business, day, start, end);
        business.getBusinessHours().add(hours);
        return business;
    }
}