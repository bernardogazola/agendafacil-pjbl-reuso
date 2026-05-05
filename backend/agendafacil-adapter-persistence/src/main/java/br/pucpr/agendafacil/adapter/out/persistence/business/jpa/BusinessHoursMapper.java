package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter horários de funcionamento entre o modelo de
 * domínio e a entidade JPA.
 *
 * <p>Essa classe mantém separada a representação usada pelas regras de negócio
 * da representação usada pela persistência.</p>
 *
 * <p>Ao converter para domínio, o estabelecimento relacionado é preenchido
 * apenas com o id. Isso evita carregar o grafo completo de objetos quando o
 * caso de uso precisa somente da referência ao estabelecimento.</p>
 */
public final class BusinessHoursMapper {

    private BusinessHoursMapper() {}

    /**
     * Converte uma entidade JPA de horário de funcionamento para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return horário de funcionamento correspondente ou {@code null} quando a entrada for nula
     */
    public static BusinessHours toDomain(BusinessHoursJpaEntity jpa) {
        if (jpa == null) return null;
        BusinessHours h = new BusinessHours();
        h.setId(jpa.getId());
        h.setDayOfWeek(jpa.getDayOfWeek());
        h.setStartTime(jpa.getStartTime());
        h.setEndTime(jpa.getEndTime());
        if (jpa.isActive()) h.activate(); else h.deactivate();

        if (jpa.getBusiness() != null) {
            Business stub = new Business();
            stub.setId(jpa.getBusiness().getId());
            h.setBusiness(stub);
        }
        return h;
    }

    /**
     * Converte um horário de funcionamento do domínio para a entidade JPA.
     *
     * <p>Quando o horário já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * @param h horário de funcionamento que será convertido
     * @param em gerenciador de entidades usado para buscar referências existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static BusinessHoursJpaEntity toJpa(BusinessHours h, EntityManager em) {
        if (h == null) return null;
        BusinessHoursJpaEntity jpa = h.getId() != null
                ? em.find(BusinessHoursJpaEntity.class, h.getId())
                : null;
        if (jpa == null) jpa = new BusinessHoursJpaEntity();
        jpa.setDayOfWeek(h.getDayOfWeek());
        jpa.setStartTime(h.getStartTime());
        jpa.setEndTime(h.getEndTime());
        jpa.setActive(h.isActive());
        if (h.getBusiness() != null && h.getBusiness().getId() != null) {
            jpa.setBusiness(em.getReference(BusinessJpaEntity.class, h.getBusiness().getId()));
        }
        return jpa;
    }
}