package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import jakarta.persistence.*;

/**
 * Entidade JPA que representa um administrador no banco de dados.
 *
 * <p>Esta classe especializa {@link UserJpaEntity} usando a estratégia de
 * herança {@code JOINED}. Os dados comuns do usuário ficam na tabela
 * {@code users}, enquanto os dados específicos do administrador ficam na tabela
 * {@code administrators}.</p>
 *
 * <p>O nível de acesso define as permissões do administrador dentro da
 * plataforma.</p>
 */
@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "id")
public class AdministratorJpaEntity extends UserJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 30)
    private AccessLevel accessLevel;

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}
