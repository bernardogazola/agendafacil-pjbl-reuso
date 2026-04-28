package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Estratégia de disponibilidade baseada em intervalos configuráveis em minutos.
 *
 * <p>É útil para negócios que trabalham com procedimentos de durações variadas,
 * como clínicas, estéticas e atendimentos semelhantes. O tamanho do intervalo é
 * definido na criação da estratégia.</p>
 */
public class SlotBasedAvailability implements AvailabilityStrategy {

    private final int slotMinutes;

    public SlotBasedAvailability(int slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

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
            current = current.plusMinutes(slotMinutes);
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
