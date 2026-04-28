package br.pucpr.agendafacil.domain.business;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa uma janela de funcionamento de um {@link Business} em um dia da
 * semana.
 *
 * <p>Cada registro indica o dia, horário de abertura, horário de fechamento e
 * se aquela janela está ativa. A restrição de unicidade impede que o mesmo
 * estabelecimento tenha mais de uma janela cadastrada para o mesmo dia.</p>
 */
@Entity
@Table(
        name = "business_hours",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_business_hours_business_day",
                columnNames = {"business_id", "day_of_week"}
        )
)
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @NotNull(message = "O dia da semana é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "O horário de abertura é obrigatório")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "O horário de fechamento é obrigatório")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    public BusinessHours() {
    }

    public BusinessHours(Business business, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.business = business;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessHours other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BusinessHours{id=" + id + ", dayOfWeek=" + dayOfWeek
                + ", startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}
