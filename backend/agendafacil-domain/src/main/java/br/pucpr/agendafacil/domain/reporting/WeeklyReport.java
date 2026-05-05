package br.pucpr.agendafacil.domain.reporting;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Relatório semanal de agendamentos.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link PeriodReport}. O período semanal começa na segunda-feira da semana da
 * data de referência e termina no domingo.</p>
 */
public class WeeklyReport extends PeriodReport {

    @Override
    protected LocalDate calculateStart(LocalDate reference) {
        return reference.with(DayOfWeek.MONDAY);
    }

    @Override
    protected LocalDate calculateEnd(LocalDate reference) {
        return calculateStart(reference).plusDays(6);
    }
}
