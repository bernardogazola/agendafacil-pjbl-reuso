package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Política usada para calcular o preço de um agendamento.
 *
 * <p>Esta interface representa o ponto variável do padrão Strategy: cada
 * serviço pode ter uma forma diferente de calcular o preço final, sem que o
 * código responsável pelo agendamento precise conhecer os detalhes dessa
 * regra.</p>
 *
 * <p>O arredondamento e a formatação monetária devem ficar fora da política de
 * preço. Esta interface apenas calcula o valor bruto conforme a regra definida
 * para o serviço.</p>
 */
public interface PricingPolicy {

    double calculate(OfferedService service, Appointment appointment);
}
