package br.pucpr.agendafacil.domain.reporting;

/**
 * Período disponível para geração de relatórios.
 *
 * <p>Cada valor representa uma janela de análise diferente, como relatório
 * diário, semanal ou mensal. A escolha do período define qual implementação de
 * {@link PeriodReport} será usada.</p>
 */
public enum ReportPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}
