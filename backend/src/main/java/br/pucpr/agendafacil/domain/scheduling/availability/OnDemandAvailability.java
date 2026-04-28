package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Estratégia usada quando o estabelecimento não trabalha com uma grade fixa
 * de horários disponíveis.
 *
 * <p>Nesse caso, a lista vazia não representa necessariamente falta de agenda,
 * mas sim que o horário deve ser combinado diretamente com o cliente.</p>
 */
public class OnDemandAvailability implements AvailabilityStrategy {

    @Override
    public List<LocalTime> availableSlots(Business business, LocalDate day) {
        return Collections.emptyList();
    }
}
