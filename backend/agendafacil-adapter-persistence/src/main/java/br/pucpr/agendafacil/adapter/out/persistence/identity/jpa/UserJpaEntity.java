package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA base para os usuários do sistema.
 *
 * <p>Esta classe representa os dados comuns entre os tipos de usuário, como
 * nome, e-mail, senha, telefone, situação ativa e datas de auditoria.</p>
 *
 * <p>A herança usa a estratégia {@link InheritanceType#JOINED}: os campos
 * comuns ficam na tabela {@code users}, enquanto os dados específicos ficam
 * nas tabelas dos subtipos, como clientes e administradores.</p>
 *
 * <p>As datas de criação e atualização são controladas nesta classe por meio
 * dos métodos {@link #onCreate()} e {@link #onUpdate()}, evitando duplicação
 * dessa lógica nas entidades filhas.</p>
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Define as datas de criação e atualização antes da primeira persistência.
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Atualiza a data de modificação antes de salvar alterações na entidade.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}