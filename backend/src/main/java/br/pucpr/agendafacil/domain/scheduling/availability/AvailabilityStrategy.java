package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Estratégia responsável por calcular os horários disponíveis de um
 * estabelecimento em um determinado dia.
 *
 * <p>Esta interface representa o ponto variável do padrão Strategy: cada tipo
 * de negócio pode ter uma regra diferente para gerar horários, sem que a classe
 * que consulta a agenda precise conhecer esses detalhes.</p>
 *
 * <p>Exemplos de variação incluem horários fixos de uma hora, intervalos
 * configuráveis em minutos ou atendimentos sem horários previamente definidos.</p>
 */
public interface AvailabilityStrategy {

    List<LocalTime> availableSlots(Business business, LocalDate day);
}
