package br.pucpr.agendafacil.domain.business.port;

import br.pucpr.agendafacil.domain.business.OfferedService;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link OfferedService}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir serviços oferecidos por um
 * estabelecimento, sem conhecer os detalhes da tecnologia usada para armazenar
 * os dados.</p>
 */
public interface OfferedServiceRepository {

    /**
     * Busca um serviço pelo id, esteja ele ativo ou não.
     *
     * @param id identificador do serviço
     * @return o serviço encontrado ou {@code null} quando não existir
     */
    OfferedService getById(Long id);

    /**
     * Busca um serviço ativo pelo id.
     *
     * @param id identificador do serviço
     * @return o serviço ativo encontrado, quando existir
     */
    Optional<OfferedService> findActiveById(Long id);

    /**
     * Busca todos os serviços de um estabelecimento, ativos ou não.
     *
     * @param businessId identificador do estabelecimento
     * @return lista de serviços cadastrados para o estabelecimento
     */
    List<OfferedService> findByBusinessId(Long businessId);

    /**
     * Busca apenas os serviços ativos de um estabelecimento.
     *
     * <p>Esse método é usado na descoberta pública de serviços disponíveis para
     * agendamento.</p>
     *
     * @param businessId identificador do estabelecimento
     * @return lista de serviços ativos do estabelecimento
     */
    List<OfferedService> findActiveByBusinessId(Long businessId);

    /**
     * Persiste um novo serviço ou atualiza um serviço existente.
     *
     * @param service serviço a ser salvo
     */
    void persist(OfferedService service);

    /**
     * Atualiza um serviço existente.
     *
     * <p>A implementação deve localizar o registro pelo id do serviço informado e
     * aplicar os novos dados.</p>
     *
     * @param service serviço com os dados atualizados
     * @return serviço atualizado
     */
    OfferedService update(OfferedService service);
}