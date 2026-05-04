package br.pucpr.agendafacil.domain.scheduling;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.scheduling.availability.AvailabilityStrategy;
import br.pucpr.agendafacil.domain.scheduling.cache.AppointmentRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Representa a agenda de um estabelecimento em um intervalo de datas.
 *
 * <p>Esta classe não é uma entidade persistida. Ela funciona como uma visão
 * montada a partir de um {@link Business}, de um período e dos agendamentos já
 * conhecidos nesse intervalo.</p>
 *
 * <p>A agenda combina as regras de disponibilidade do estabelecimento com os
 * agendamentos existentes para informar horários disponíveis e detectar
 * conflitos.</p>
 */
public class Schedule {

    private final Business business;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final List<Appointment> appointments;

    public Schedule(Business business, LocalDate periodStart, LocalDate periodEnd,
                    List<Appointment> appointments) {
        this.business = Objects.requireNonNull(business, "business");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd");
        this.appointments = appointments == null ? new ArrayList<>() : new ArrayList<>(appointments);
    }

    public Business getBusiness() {
        return business;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public List<Appointment> getAppointments() {
        return List.copyOf(appointments);
    }

    /**
     * Calcula os horários disponíveis para uma data usando a estratégia
     * informada.
     *
     * <p>Este método atua como contexto do padrão Strategy: a
     * {@link AvailabilityStrategy} gera os horários candidatos, e a agenda
     * remove aqueles que já estão ocupados por agendamentos ativos.</p>
     *
     * @param day data consultada
     * @param strategy estratégia usada para gerar os horários candidatos
     * @return lista de horários disponíveis no dia informado
     */
    public List<LocalTime> availableSlots(LocalDate day, AvailabilityStrategy strategy) {
        List<LocalTime> candidates = strategy.availableSlots(business, day);

        Set<LocalTime> occupied = appointments.stream()
                .filter(Appointment::isActive)
                .filter(a -> a.getScheduledAt().toLocalDate().equals(day))
                .map(a -> a.getScheduledAt().toLocalTime())
                .collect(Collectors.toSet());

        return candidates.stream()
                .filter(slot -> !occupied.contains(slot))
                .toList();
    }

    /**
     * Verifica se o horário informado entra em conflito com algum agendamento
     * ativo do mesmo estabelecimento.
     *
     * <p>A verificação considera tanto os agendamentos carregados nesta agenda
     * quanto os agendamentos presentes no cache global
     * {@link AppointmentRegistry}.</p>
     *
     * @param candidateStart início pretendido para o novo agendamento
     * @param durationMinutes duração do novo agendamento em minutos
     * @return {@code true} se houver sobreposição com outro agendamento ativo
     */
    public boolean hasConflict(LocalDateTime candidateStart, int durationMinutes) {
        LocalDateTime candidateEnd = candidateStart.plusMinutes(durationMinutes);
        Long businessId = business.getId();
        LocalDate day = candidateStart.toLocalDate();
        List<Appointment> cached = businessId == null
                ? List.of()
                : AppointmentRegistry.getInstance().forDay(day, businessId);
        return Stream.concat(cached.stream(), appointments.stream())
                .filter(Appointment::isActive)
                .distinct()
                .anyMatch(other -> overlaps(other, candidateStart, candidateEnd));
    }

    private static boolean overlaps(Appointment other, LocalDateTime start, LocalDateTime end) {
        LocalDateTime otherStart = other.getScheduledAt();
        LocalDateTime otherEnd = otherStart.plusMinutes(other.getEstimatedDurationMinutes());
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }
}