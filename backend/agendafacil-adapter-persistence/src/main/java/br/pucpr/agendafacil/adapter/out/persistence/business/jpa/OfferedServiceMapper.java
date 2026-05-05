package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter serviços oferecidos entre o modelo de
 * domínio e a entidade JPA.
 *
 * <p>Essa classe mantém separada a representação usada pelo domínio da
 * representação usada pela persistência.</p>
 *
 * <p>Na conversão para domínio, o estabelecimento relacionado é preenchido
 * apenas com o id. Isso evita carregar o estabelecimento completo quando o caso
 * de uso precisa somente validar a qual negócio o serviço pertence.</p>
 */
public final class OfferedServiceMapper {

    private OfferedServiceMapper() {}

    /**
     * Converte uma entidade JPA de serviço oferecido para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return serviço correspondente ou {@code null} quando a entrada for nula
     */
    public static OfferedService toDomain(OfferedServiceJpaEntity jpa) {
        if (jpa == null) return null;
        OfferedService s = new OfferedService();
        s.setId(jpa.getId());
        s.setName(jpa.getName());
        s.setDescription(jpa.getDescription());
        s.setBasePrice(jpa.getBasePrice());
        s.setDurationMinutes(jpa.getDurationMinutes());
        s.setCategory(jpa.getCategory());
        s.setPricingPolicyType(jpa.getPricingPolicyType());
        if (jpa.isActive()) s.activate(); else s.deactivate();

        if (jpa.getBusiness() != null) {
            Business stub = new Business();
            stub.setId(jpa.getBusiness().getId());
            s.setBusiness(stub);
        }
        return s;
    }

    /**
     * Converte um serviço oferecido do domínio para a entidade JPA correspondente.
     *
     * <p>Quando o serviço já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * @param s serviço que será convertido
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static OfferedServiceJpaEntity toJpa(OfferedService s, EntityManager em) {
        if (s == null) return null;
        OfferedServiceJpaEntity jpa = s.getId() != null
                ? em.find(OfferedServiceJpaEntity.class, s.getId())
                : null;
        if (jpa == null) jpa = new OfferedServiceJpaEntity();
        jpa.setName(s.getName());
        jpa.setDescription(s.getDescription());
        jpa.setBasePrice(s.getBasePrice());
        jpa.setDurationMinutes(s.getDurationMinutes());
        jpa.setCategory(s.getCategory());
        jpa.setPricingPolicyType(s.getPricingPolicyType());
        jpa.setActive(s.isActive());
        if (s.getBusiness() != null && s.getBusiness().getId() != null) {
            jpa.setBusiness(em.getReference(BusinessJpaEntity.class, s.getBusiness().getId()));
        }
        return jpa;
    }
}