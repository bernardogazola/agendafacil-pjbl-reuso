package br.pucpr.agendafacil.domain.reporting;

import java.time.LocalDate;

/**
 * Relatório mensal de agendamentos.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link PeriodReport}. O período mensal começa no primeiro dia do mês da data
 * de referência e termina no último dia desse mesmo mês.</p>
 */
public class MonthlyReport extends PeriodReport {

    @Override
    protected LocalDate calculateStart(LocalDate reference) {
        return reference.withDayOfMonth(1);
    }

    @Override
    protected LocalDate calculateEnd(LocalDate reference) {
        return calculateStart(reference).plusMonths(1).minusDays(1);
    }
}