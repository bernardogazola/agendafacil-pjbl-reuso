package br.pucpr.agendafacil.domain.business;

import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa um estabelecimento cadastrado na plataforma.
 *
 * <p>Um estabelecimento possui dados básicos de identificação, categoria,
 * plano contratado, responsável administrativo, serviços oferecidos e horários
 * de funcionamento.</p>
 *
 * <p>A categoria do estabelecimento influencia algumas regras de agendamento,
 * como o processador usado para aplicar validações específicas. A política de
 * cancelamento configurada também define qual regra será usada quando um
 * cliente solicitar o cancelamento de um agendamento.</p>
 */
public class Business {

    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Size(max = 200)
    private String tradeName;

    @Size(max = 20)
    private String taxId;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 160)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotNull(message = "A categoria é obrigatória")
    private BusinessCategory category;

    @NotNull(message = "O plano é obrigatório")
    private BusinessPlan plan;

    /**
     * Política de cancelamento configurada para este estabelecimento.
     *
     * <p>Esse valor é usado para escolher a implementação de
     * {@code CancellationPolicy} aplicada quando um cliente solicita o
     * cancelamento de um agendamento.</p>
     */
    @NotNull
    private CancellationPolicyType cancellationPolicyType = CancellationPolicyType.FREE;

    private boolean active = true;

    @NotNull(message = "O responsável é obrigatório")
    private Administrator owner;

    private List<OfferedService> offeredServices = new ArrayList<>();

    private List<BusinessHours> businessHours = new ArrayList<>();

    public Business() {
    }

    public Business(String tradeName, String email, BusinessCategory category,
                    BusinessPlan plan, Administrator owner) {
        this.tradeName = tradeName;
        this.email = email;
        this.category = category;
        this.plan = plan;
        this.owner = owner;
    }

    public Long getId() {
        return id;
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

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public Administrator getOwner() {
        return owner;
    }

    public void setOwner(Administrator owner) {
        this.owner = owner;
    }

    public List<OfferedService> getOfferedServices() {
        return offeredServices;
    }

    public List<BusinessHours> getBusinessHours() {
        return businessHours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Business other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Business{id=" + id + ", tradeName='" + tradeName + "', category=" + category + "}";
    }
}
