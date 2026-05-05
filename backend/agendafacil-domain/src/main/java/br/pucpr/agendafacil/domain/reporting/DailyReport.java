package br.pucpr.agendafacil.domain.reporting;

import java.time.LocalDate;

/**
 * Relatório diário de agendamentos.
 *
 * <p>Esta classe é uma implementação concreta do Template Method definido em
 * {@link PeriodReport}. Para o relatório diário, a data inicial e a data final
 * são iguais à data de referência.</p>
 */
public class DailyReport extends PeriodReport {

    @Override
    protected LocalDate calculateStart(LocalDate reference) {
        return reference;
    }

    @Override
    protected LocalDate calculateEnd(LocalDate reference) {
        return reference;
    }
}
