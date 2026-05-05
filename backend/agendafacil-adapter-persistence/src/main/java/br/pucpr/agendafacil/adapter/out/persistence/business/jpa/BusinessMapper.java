package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.AdministratorJpaEntity;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.identity.Administrator;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter estabelecimentos entre o modelo de domínio
 * e a entidade JPA.
 *
 * <p>Essa classe isola a conversão entre a camada de domínio e a camada de
 * persistência, evitando que as regras de negócio dependam diretamente das
 * entidades JPA.</p>
 *
 * <p>Na conversão para domínio, o responsável pelo estabelecimento é preenchido
 * como um objeto parcial com id. As coleções de serviços e horários não são
 * carregadas aqui, pois possuem repositórios próprios para consultas mais
 * específicas.</p>
 */
public final class BusinessMapper {

    private BusinessMapper() {}

    /**
     * Converte uma entidade JPA de estabelecimento para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return estabelecimento correspondente ou {@code null} quando a entrada for nula
     */
    public static Business toDomain(BusinessJpaEntity jpa) {
        if (jpa == null) return null;
        Business b = new Business();
        b.setId(jpa.getId());
        b.setTradeName(jpa.getTradeName());
        b.setTaxId(jpa.getTaxId());
        b.setEmail(jpa.getEmail());
        b.setPhone(jpa.getPhone());
        b.setCategory(jpa.getCategory());
        b.setPlan(jpa.getPlan());
        b.setCancellationPolicyType(jpa.getCancellationPolicyType());
        if (jpa.isActive()) b.activate(); else b.deactivate();

        if (jpa.getOwner() != null) {
            Administrator ownerStub = new Administrator();
            ownerStub.setId(jpa.getOwner().getId());
            // quando os dados do responsável já estiverem disponíveis, aproveita nome e e-mail
            // Se a entidade estiver apenas como proxy lazy, o id já é suficiente para o domínio
            try {
                ownerStub.setName(jpa.getOwner().getName());
                ownerStub.setEmail(jpa.getOwner().getEmail());
            } catch (RuntimeException lazyMiss) {
                // Mantém apenas o id para evitar forçar carregamento do relacionamento
            }
            b.setOwner(ownerStub);
        }
        return b;
    }

    /**
     * Converte um estabelecimento do domínio para a entidade JPA correspondente.
     *
     * <p>Quando o estabelecimento já possui id, o método tenta reaproveitar a
     * entidade existente no {@link EntityManager}. Caso contrário, cria uma nova
     * entidade para persistência.</p>
     *
     * @param b estabelecimento que será convertido
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static BusinessJpaEntity toJpa(Business b, EntityManager em) {
        if (b == null) return null;
        BusinessJpaEntity jpa = b.getId() != null
                ? em.find(BusinessJpaEntity.class, b.getId())
                : null;
        if (jpa == null) jpa = new BusinessJpaEntity();
        jpa.setTradeName(b.getTradeName());
        jpa.setTaxId(b.getTaxId());
        jpa.setEmail(b.getEmail());
        jpa.setPhone(b.getPhone());
        jpa.setCategory(b.getCategory());
        jpa.setPlan(b.getPlan());
        jpa.setCancellationPolicyType(b.getCancellationPolicyType());
        jpa.setActive(b.isActive());
        if (b.getOwner() != null && b.getOwner().getId() != null) {
            jpa.setOwner(em.getReference(AdministratorJpaEntity.class, b.getOwner().getId()));
        }
        return jpa;
    }
}