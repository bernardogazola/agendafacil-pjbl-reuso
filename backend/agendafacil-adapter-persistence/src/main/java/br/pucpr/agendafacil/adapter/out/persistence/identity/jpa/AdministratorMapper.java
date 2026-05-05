package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import br.pucpr.agendafacil.domain.identity.Administrator;
import jakarta.persistence.EntityManager;

/**
 * Mapper responsável por converter administradores entre o modelo de domínio e
 * a entidade JPA.
 *
 * <p>Essa classe isola a conversão entre a camada de domínio e a camada de
 * persistência, evitando que as regras de negócio dependam diretamente das
 * entidades JPA.</p>
 */
public final class AdministratorMapper {

    private AdministratorMapper() {}

    /**
     * Converte uma entidade JPA de administrador para o modelo de domínio.
     *
     * @param jpa entidade JPA que será convertida
     * @return administrador de domínio correspondente ou {@code null} quando a entrada for nula
     */
    public static Administrator toDomain(AdministratorJpaEntity jpa) {
        if (jpa == null) return null;
        Administrator a = new Administrator();
        a.setId(jpa.getId());
        a.setName(jpa.getName());
        a.setEmail(jpa.getEmail());
        a.setPassword(jpa.getPassword());
        a.setPhone(jpa.getPhone());
        if (jpa.isActive()) a.activate(); else a.deactivate();
        a.setAccessLevel(jpa.getAccessLevel());
        return a;
    }

    /**
     * Converte um administrador de domínio para a entidade JPA correspondente.
     *
     * <p>Quando o administrador já possui id, o método tenta reaproveitar a entidade
     * existente no {@link EntityManager}. Caso contrário, cria uma nova entidade
     * para persistência.</p>
     *
     * @param a administrador de domínio que será convertido
     * @param em gerenciador de entidades usado para buscar registros existentes
     * @return entidade JPA correspondente ou {@code null} quando a entrada for nula
     */
    public static AdministratorJpaEntity toJpa(Administrator a, EntityManager em) {
        if (a == null) return null;
        AdministratorJpaEntity jpa = a.getId() != null
                ? em.find(AdministratorJpaEntity.class, a.getId())
                : null;
        if (jpa == null) jpa = new AdministratorJpaEntity();
        jpa.setName(a.getName());
        jpa.setEmail(a.getEmail());
        jpa.setPassword(a.getPassword());
        jpa.setPhone(a.getPhone());
        jpa.setActive(a.isActive());
        jpa.setAccessLevel(a.getAccessLevel());
        return jpa;
    }
}
