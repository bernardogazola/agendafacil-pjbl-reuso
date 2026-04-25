package br.pucpr.agendafacil.domain.identity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Usuário com permissão administrativa, gerencia empresas, serviços e relatórios.
 */
@Entity
@Table(name = "administrators")
public class Administrator extends User {

    @NotNull(message = "O nível de acesso é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 30)
    private AccessLevel accessLevel;

    public Administrator() {
    }

    public Administrator(String name, String email, String password, AccessLevel accessLevel) {
        super(name, email, password);
        this.accessLevel = accessLevel;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}