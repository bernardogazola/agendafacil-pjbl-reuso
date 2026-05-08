package br.pucpr.agendafacil.domain.business.port;

import br.pucpr.agendafacil.domain.business.Business;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link Business}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir estabelecimentos, sem
 * conhecer os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface BusinessRepository {

    /**
     * Busca um estabelecimento pelo id, esteja ele ativo ou não.
     *
     * @param id identificador do estabelecimento
     * @return o estabelecimento encontrado ou {@code null} quando não existir
     */
    Business getById(Long id);

    /**
     * Busca um estabelecimento ativo pelo id.
     *
     * @param id identificador do estabelecimento
     * @return o estabelecimento ativo encontrado, quando existir
     */
    Optional<Business> findActiveById(Long id);

    /**
     * Busca os estabelecimentos administrados por um mesmo responsável.
     *
     * @param ownerId identificador do responsável pelo estabelecimento
     * @return lista de estabelecimentos pertencentes ao responsável informado
     */
    List<Business> findByOwnerId(Long ownerId);

    /**
     * Busca todos os estabelecimentos ativos.
     *
     * <p>Esse método é usado na descoberta pública de estabelecimentos pelos
     * clientes.</p>
     *
     * @return lista de estabelecimentos ativos
     */
    List<Business> listActive();

    /**
     * Lista os estabelecimentos cadastrados na plataforma para uso administrativo.
     *
     * <p>Quando {@code activeOnly} for {@code true}, retorna apenas
     * estabelecimentos ativos.</p>
     *
     * @param activeOnly indica se a busca deve considerar apenas estabelecimentos ativos
     * @return lista de estabelecimentos encontrados
     */
    List<Business> listAllAdmin(boolean activeOnly);

    /**
     * Persiste um novo estabelecimento ou atualiza um estabelecimento existente.
     *
     * @param business estabelecimento a ser salvo
     */
    void persist(Business business);

    /**
     * Atualiza um estabelecimento existente.
     *
     * <p>A implementação deve localizar o registro pelo id do estabelecimento
     * informado e aplicar os novos dados.</p>
     *
     * @param business estabelecimento com os dados atualizados
     * @return estabelecimento atualizado
     */
    Business update(Business business);
}