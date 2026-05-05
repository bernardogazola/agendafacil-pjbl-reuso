package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.Promotion;
import jakarta.persistence.EntityManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Mapper responsável por converter promoções entre o modelo de domínio e a
 * entidade JPA.
 *
 * <p>Na conversão para domínio, o estabelecimento e os serviços elegíveis são
 * preenchidos como objetos parciais com id. Isso evita carregar o grafo completo
 * quando apenas as referências são necessárias.</p>
 */
public final class PromotionMapper {

    private PromotionMapper() {}

    /**
     * Converte uma entidade JPA de promoção para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return promoção correspondente ou {@code null} quando a entrada for nula
     */
    public static Promotion toDomain(PromotionJpaEntity jpa) {
        if (jpa == null) return null;
        Promotion p = new Promotion();
        p.setId(jpa.getId());
        p.setName(jpa.getName());
        p.setDescription(jpa.getDescription());
        p.setDiscountPercentage(jpa.getDiscountPercentage());
        p.setDiscountAmount(jpa.getDiscountAmount());
        p.setValidFrom(jpa.getValidFrom());
        p.setValidTo(jpa.getValidTo());
        if (jpa.isActive()) p.activate(); else p.deactivate();

        if (jpa.getBusiness() != null) {
            Business stub = new Business();
            stub.setId(jpa.getBusiness().getId());
            p.setBusiness(stub);
        }

        Set<OfferedService> eligibles = new HashSet<>();
        if (jpa.getEligibleServices() != null) {
            for (OfferedServiceJpaEntity osJpa : jpa.getEligibleServices()) {
                OfferedService stub = new OfferedService();
                stub.setId(osJpa.getId());
                eligibles.add(stub);
            }
        }
        p.setEligibleServices(eligibles);
        return p;
    }

    /**
     * Converte uma promoção do domínio para a entidade JPA correspondente.
     *
     * <p>Quando a promoção já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. As associações com estabelecimento e
     * serviços elegíveis são feitas por referência.</p>
     *
     * @param p promoção que será convertida
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static PromotionJpaEntity toJpa(Promotion p, EntityManager em) {
        if (p == null) return null;
        PromotionJpaEntity jpa = p.getId() != null
                ? em.find(PromotionJpaEntity.class, p.getId())
                : null;
        if (jpa == null) jpa = new PromotionJpaEntity();
        jpa.setName(p.getName());
        jpa.setDescription(p.getDescription());
        jpa.setDiscountPercentage(p.getDiscountPercentage());
        jpa.setDiscountAmount(p.getDiscountAmount());
        jpa.setValidFrom(p.getValidFrom());
        jpa.setValidTo(p.getValidTo());
        jpa.setActive(p.isActive());
        if (p.getBusiness() != null && p.getBusiness().getId() != null) {
            jpa.setBusiness(em.getReference(BusinessJpaEntity.class, p.getBusiness().getId()));
        }

        Set<OfferedServiceJpaEntity> eligibles = new HashSet<>();
        if (p.getEligibleServices() != null) {
            for (OfferedService os : p.getEligibleServices()) {
                if (os.getId() != null) {
                    eligibles.add(em.getReference(OfferedServiceJpaEntity.class, os.getId()));
                }
            }
        }
        jpa.setEligibleServices(eligibles);
        return jpa;
    }
}