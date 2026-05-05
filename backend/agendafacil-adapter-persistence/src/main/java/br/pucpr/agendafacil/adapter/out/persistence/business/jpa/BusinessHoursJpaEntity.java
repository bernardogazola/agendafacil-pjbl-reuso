package br.pucpr.agendafacil.adapter.out.persistence.business.jpa;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Entidade JPA que representa a janela de funcionamento de um estabelecimento.
 *
 * <p>Cada registro define o dia da semana, o horário de abertura, o horário de
 * fechamento e se a janela está ativa.</p>
 *
 * <p>A restrição de unicidade impede que o mesmo estabelecimento tenha mais de
 * uma janela cadastrada para o mesmo dia da semana.</p>
 */
@Entity
@Table(
        name = "business_hours",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_business_hours_business_day",
                columnNames = {"business_id", "day_of_week"}
        )
)
public class BusinessHoursJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private BusinessJpaEntity business;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public BusinessJpaEntity getBusiness() {
        return business;
    }
    public void setBusiness(BusinessJpaEntity business) {
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
    public void setActive(boolean active) {
        this.active = active;
    }
}