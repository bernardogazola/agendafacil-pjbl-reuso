package br.pucpr.agendafacil.domain.business.port;

import br.pucpr.agendafacil.domain.business.Promotion;

import java.util.List;

/**
 * Repositório responsável pelo acesso aos dados de {@link Promotion}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir campanhas promocionais, sem
 * conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface PromotionRepository {

    /**
     * Busca uma promoção pelo id.
     *
     * @param id identificador da promoção
     * @return a promoção encontrada ou {@code null} quando não existir
     */
    Promotion getById(Long id);

    /**
     * Busca as promoções de um estabelecimento.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de promoções cadastradas para o estabelecimento
     */
    List<Promotion> findByBusinessId(Long businessId);

    /**
     * Persiste uma nova promoção.
     *
     * @param promotion promoção a ser salva
     */
    void persist(Promotion promotion);

    /**
     * Atualiza uma promoção existente.
     *
     * <p>A implementação deve localizar o registro pelo id da promoção
     * informada e aplicar os novos dados.</p>
     *
     * @param promotion promoção com os dados atualizados
     * @return promoção atualizada
     */
    Promotion update(Promotion promotion);
}