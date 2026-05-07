package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.CustomerResponse;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.EnumSet;
import java.util.Set;

/**
 * Mapper responsável por converter clientes para DTOs de resposta.
 */
@ApplicationScoped
public class CustomerMapper {

    /**
     * Converte um cliente em sua representação de resposta.
     *
     * @param c cliente que será convertido
     * @return DTO com os dados principais do cliente
     */
    public CustomerResponse toResponse(Customer c) {
        Set<NotificationChannel> prefs = c.getNotificationPreferences();
        return new CustomerResponse(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getPhone(),
                c.getBirthDate(),
                prefs == null || prefs.isEmpty()
                        ? EnumSet.noneOf(NotificationChannel.class)
                        : EnumSet.copyOf(prefs),
                c.isActive()
        );
    }
}