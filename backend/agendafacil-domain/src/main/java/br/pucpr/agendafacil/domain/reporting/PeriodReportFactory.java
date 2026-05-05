package br.pucpr.agendafacil.domain.reporting;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

/**
 * Factory responsável por escolher o relatório adequado para cada período.
 *
 * <p>Ela complementa o Template Method usado em {@link PeriodReport}: o fluxo
 * geral de geração fica na classe abstrata, enquanto esta factory escolhe a
 * implementação concreta para relatório diário, semanal ou mensal.</p>
 *
 * <p>Além disso, a factory conecta o relatório ao {@link AppointmentRepository}
 * sobrescrevendo o método de busca dos agendamentos. Assim, as classes de
 * relatório continuam focadas no cálculo da janela e na geração do resultado.</p>
 */
@ApplicationScoped
public class PeriodReportFactory {

    /**
     * Retorna o relatório correspondente ao período informado.
     *
     * <p>O método também adapta a busca dos agendamentos para usar o
     * repositório recebido.</p>
     *
     * @param period período desejado para o relatório
     * @param repository repositório usado para buscar os agendamentos
     * @return relatório correspondente ao período informado
     */
    public PeriodReport resolve(ReportPeriod period, AppointmentRepository repository) {
        return switch (period) {
            case DAILY -> new DailyReport() {
                @Override
                protected List<Appointment> fetch(Long businessId, LocalDate start, LocalDate end) {
                    return repository.findByBusinessAndDateRange(businessId, start, end);
                }
            };
            case WEEKLY -> new WeeklyReport() {
                @Override
                protected List<Appointment> fetch(Long businessId, LocalDate start, LocalDate end) {
                    return repository.findByBusinessAndDateRange(businessId, start, end);
                }
            };
            case MONTHLY -> new MonthlyReport() {
                @Override
                protected List<Appointment> fetch(Long businessId, LocalDate start, LocalDate end) {
                    return repository.findByBusinessAndDateRange(businessId, start, end);
                }
            };
        };
    }

    /**
     * Calcula a data inicial da janela de um período.
     *
     * @param period período desejado
     * @param reference data usada como referência
     * @return data inicial da janela
     */
    public LocalDate windowStart(ReportPeriod period, LocalDate reference) {
        return switch (period) {
            case DAILY -> reference;
            case WEEKLY -> reference.with(java.time.DayOfWeek.MONDAY);
            case MONTHLY -> reference.withDayOfMonth(1);
        };
    }

    /**
     * Calcula a data final da janela de um período.
     *
     * @param period período desejado
     * @param reference data usada como referência
     * @return data final da janela
     */
    public LocalDate windowEnd(ReportPeriod period, LocalDate reference) {
        return switch (period) {
            case DAILY -> reference;
            case WEEKLY -> windowStart(period, reference).plusDays(6);
            case MONTHLY -> reference.withDayOfMonth(1).plusMonths(1).minusDays(1);
        };
    }
}
