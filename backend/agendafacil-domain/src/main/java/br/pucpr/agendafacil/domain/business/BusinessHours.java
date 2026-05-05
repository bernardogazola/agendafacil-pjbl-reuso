package br.pucpr.agendafacil.domain.business;

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
public class BusinessHours {

    private Long id;

    @NotNull
    private Business business;

    @NotNull(message = "O dia da semana é obrigatório")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "O horário de abertura é obrigatório")
    private LocalTime startTime;

    @NotNull(message = "O horário de fechamento é obrigatório")
    private LocalTime endTime;

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

    public void setId(Long id) {
        this.id = id;
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
