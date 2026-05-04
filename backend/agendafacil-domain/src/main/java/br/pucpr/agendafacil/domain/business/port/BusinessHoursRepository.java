package br.pucpr.agendafacil.domain.business.port;

import br.pucpr.agendafacil.domain.business.BusinessHours;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link BusinessHours}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir horários de funcionamento,
 * sem conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface BusinessHoursRepository {

    /**
     * Busca os horários de funcionamento de um estabelecimento.
     *
     * <p>Normalmente retorna as janelas cadastradas para os dias da semana do
     * estabelecimento.</p>
     *
     * @param businessId identificador do estabelecimento
     * @return lista de horários de funcionamento encontrados
     */
    List<BusinessHours> findByBusinessId(Long businessId);

    /**
     * Busca o horário de funcionamento de um estabelecimento em um dia
     * específico da semana.
     *
     * @param businessId identificador do estabelecimento
     * @param day dia da semana consultado
     * @return horário encontrado para o dia informado, quando existir
     */
    Optional<BusinessHours> findByBusinessAndDayOfWeek(Long businessId, DayOfWeek day);

    /**
     * Persiste um horário de funcionamento.
     *
     * @param hours horário de funcionamento a ser salvo
     */
    void persist(BusinessHours hours);
}
