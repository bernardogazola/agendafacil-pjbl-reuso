package br.pucpr.agendafacil.domain.reporting;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Define o fluxo base para geração de relatórios por período.
 *
 * <p>Esta classe aplica o padrão Template Method: o método {@link #generate}
 * fixa a ordem das etapas do relatório, enquanto as subclasses definem como
 * calcular o início e o fim do período.</p>
 *
 * <p>O fluxo comum calcula a janela de datas, busca os agendamentos do período,
 * calcula as métricas e formata o resultado final. Assim, relatórios diários,
 * semanais e mensais reaproveitam a mesma estrutura, mudando apenas a janela
 * analisada.</p>
 */
public abstract class PeriodReport {

    /**
     * Gera o relatório para um estabelecimento a partir de uma data de referência.
     *
     * <p>Este é o template method da classe. Por isso, é {@code final}: as
     * subclasses podem alterar os pontos específicos do relatório, mas não a
     * ordem geral da geração.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param reference data usada como referência para calcular o período
     * @return relatório formatado
     */
    public final String generate(Long businessId, LocalDate reference) {
        LocalDate start = calculateStart(reference);
        LocalDate end = calculateEnd(reference);
        List<Appointment> appointments = fetch(businessId, start, end);
        Metrics metrics = calculateMetrics(appointments);
        return format(start, end, metrics);
    }

    /**
     * Calcula a data inicial do período do relatório.
     *
     * @param reference data de referência informada
     * @return data inicial do período
     */
    protected abstract LocalDate calculateStart(LocalDate reference);

    /**
     * Calcula a data final do período do relatório.
     *
     * @param reference data de referência informada
     * @return data final do período
     */
    protected abstract LocalDate calculateEnd(LocalDate reference);

    /**
     * Busca os agendamentos dentro do período calculado.
     *
     * <p>A implementação padrão retorna uma lista vazia. A factory pode
     * sobrescrever este método para conectar o relatório ao repositório de
     * agendamentos.</p>
     *
     * @param businessId identificador do estabelecimento
     * @param start data inicial do período
     * @param end data final do período
     * @return lista de agendamentos encontrados no período
     */
    protected List<Appointment> fetch(Long businessId, LocalDate start, LocalDate end) {
        return List.of();
    }

    /**
     * Calcula as métricas principais do relatório.
     *
     * <p>Atualmente considera a quantidade de agendamentos e a soma dos valores
     * pagos.</p>
     *
     * @param appointments agendamentos usados no cálculo
     * @return métricas calculadas para o período
     */
    protected Metrics calculateMetrics(List<Appointment> appointments) {
        BigDecimal revenue = appointments.stream()
                .map(Appointment::getPricePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new Metrics(appointments.size(), revenue);
    }

    /**
     * Formata o resultado final do relatório.
     *
     * @param start data inicial do período
     * @param end data final do período
     * @param metrics métricas calculadas
     * @return texto final do relatório
     */
    protected String format(LocalDate start, LocalDate end, Metrics metrics) {
        return String.format(
                "Relatório de %s a %s: %d atendimentos, R$ %s de receita",
                start, end, metrics.count(), metrics.revenue().toPlainString());
    }

    /**
     * Métricas calculadas para um relatório de período.
     *
     * @param count quantidade de atendimentos encontrados
     * @param revenue receita total do período
     */
    public record Metrics(int count, BigDecimal revenue) {
    }
}
