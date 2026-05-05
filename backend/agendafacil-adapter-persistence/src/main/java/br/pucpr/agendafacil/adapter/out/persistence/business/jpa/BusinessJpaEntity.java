package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import br.pucpr.agendafacil.adapter.out.persistence.identity.jpa.AdministratorJpaEntity;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um estabelecimento no banco de dados.
 *
 * <p>Esta classe armazena os dados principais do estabelecimento, como nome
 * comercial, documento, contato, categoria, plano, responsável e situação
 * ativa.</p>
 *
 * <p>Os serviços oferecidos e os horários de funcionamento são relacionados ao
 * estabelecimento por associações {@code OneToMany}. As datas de criação e
 * atualização são controladas pelos métodos de ciclo de vida da entidade.</p>
 */
@Entity
@Table(name = "businesses")
public class BusinessJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_name", nullable = false, length = 200)
    private String tradeName;

    @Column(name = "tax_id", length = 20)
    private String taxId;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private BusinessCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 30)
    private BusinessPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy_type", nullable = false, length = 30)
    private CancellationPolicyType cancellationPolicyType = CancellationPolicyType.FREE;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AdministratorJpaEntity owner;

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferedServiceJpaEntity> offeredServices = new ArrayList<>();

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHoursJpaEntity> businessHours = new ArrayList<>();

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

    public String getTradeName() {
        return tradeName;
    }
    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getTaxId() {
        return taxId;
    }
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BusinessCategory getCategory() {
        return category;
    }
    public void setCategory(BusinessCategory category) {
        this.category = category;
    }

    public BusinessPlan getPlan() {
        return plan;
    }
    public void setPlan(BusinessPlan plan) {
        this.plan = plan;
    }

    public CancellationPolicyType getCancellationPolicyType() {
        return cancellationPolicyType;
    }
    public void setCancellationPolicyType(CancellationPolicyType cancellationPolicyType) {
        this.cancellationPolicyType = cancellationPolicyType;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public AdministratorJpaEntity getOwner() {
        return owner;
    }
    public void setOwner(AdministratorJpaEntity owner) {
        this.owner = owner;
    }

    public List<OfferedServiceJpaEntity> getOfferedServices() {
        return offeredServices;
    }
    public void setOfferedServices(List<OfferedServiceJpaEntity> offeredServices) {
        this.offeredServices = offeredServices;
    }

    public List<BusinessHoursJpaEntity> getBusinessHours() {
        return businessHours;
    }
    public void setBusinessHours(List<BusinessHoursJpaEntity> businessHours) {
        this.businessHours = businessHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}