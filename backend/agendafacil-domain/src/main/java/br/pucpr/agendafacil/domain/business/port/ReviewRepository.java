package br.pucpr.agendafacil.domain.business.port;

import br.pucpr.agendafacil.domain.business.Review;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link Review}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir avaliações de atendimentos,
 * sem conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface ReviewRepository {

    /**
     * Busca uma avaliação pelo id.
     *
     * @param id identificador da avaliação
     * @return a avaliação encontrada ou {@code null} quando não existir
     */
    Review getById(Long id);

    /**
     * Busca a avaliação associada a um agendamento.
     *
     * @param appointmentId identificador do agendamento
     * @return avaliação encontrada para o agendamento, quando existir
     */
    Optional<Review> findByAppointmentId(Long appointmentId);

    /**
     * Busca as avaliações feitas por um cliente.
     *
     * @param customerId identificador do cliente
     * @return lista de avaliações do cliente
     */
    List<Review> findByCustomerId(Long customerId);

    /**
     * Busca as avaliações relacionadas aos agendamentos de um estabelecimento.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de avaliações do estabelecimento
     */
    List<Review> findByBusinessId(Long businessId);

    /**
     * Persiste uma nova avaliação.
     *
     * @param review avaliação a ser salva
     */
    void persist(Review review);

    /**
     * Atualiza uma avaliação existente.
     *
     * <p>A implementação deve localizar o registro pelo id da avaliação
     * informada e aplicar os novos dados.</p>
     *
     * @param review avaliação com os dados atualizados
     * @return avaliação atualizada
     */
    Review update(Review review);

    /**
     * Remove uma avaliação pelo id.
     *
     * @param id identificador da avaliação
     */
    void removeById(Long id);
}