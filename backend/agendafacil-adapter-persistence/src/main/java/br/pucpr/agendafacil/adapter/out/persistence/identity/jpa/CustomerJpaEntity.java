package br.pucpr.agendafacil.adapter.out.persistence.identity.jpa;

import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Entidade JPA que representa um cliente no banco de dados.
 *
 * <p>Esta classe especializa {@link UserJpaEntity} usando a estratégia de
 * herança {@code JOINED}. Os dados comuns do usuário ficam na tabela
 * {@code users}, enquanto os dados específicos do cliente ficam na tabela
 * {@code customers}.</p>
 *
 * <p>As preferências de notificação são armazenadas em uma tabela separada,
 * permitindo registrar por quais canais o cliente aceita receber mensagens.</p>
 */
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "id")
public class CustomerJpaEntity extends UserJpaEntity {

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ElementCollection(targetClass = NotificationChannel.class)
    @CollectionTable(
            name = "customer_notification_preferences",
            joinColumns = @JoinColumn(name = "customer_id")
    )
    @Column(name = "preference", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<NotificationChannel> notificationPreferences = EnumSet.noneOf(NotificationChannel.class);

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Set<NotificationChannel> getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(Set<NotificationChannel> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }
}