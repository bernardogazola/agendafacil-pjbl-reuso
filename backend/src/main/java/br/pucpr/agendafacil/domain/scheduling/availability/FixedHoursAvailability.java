package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Estratégia de disponibilidade baseada em horários fixos de uma hora.
 *
 * <p>É usada para negócios que trabalham com horários mais tradicionais, como
 * barbearias e salões, onde o cliente normalmente escolhe horários fechados
 * dentro do período de funcionamento, como 09:00, 10:00 e 11:00.</p>
 */
public class FixedHoursAvailability implements AvailabilityStrategy {

    @Override
    public List<LocalTime> availableSlots(Business business, LocalDate day) {
        BusinessHours hours = findActiveHoursFor(business, day);
        if (hours == null) {
            return List.of();
        }
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = hours.getStartTime();
        while (current.isBefore(hours.getEndTime())) {
            slots.add(current);
            current = current.plusHours(1);
        }
        return slots;
    }

    private static BusinessHours findActiveHoursFor(Business business, LocalDate day) {
        return business.getBusinessHours().stream()
                .filter(BusinessHours::isActive)
                .filter(h -> h.getDayOfWeek() == day.getDayOfWeek())
                .findFirst()
                .orElse(null);
    }
}
