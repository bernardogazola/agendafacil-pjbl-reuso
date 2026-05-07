package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.AdministratorResponse;
import br.pucpr.agendafacil.domain.identity.Administrator;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter administradores para DTOs de resposta.
 */
@ApplicationScoped
public class AdministratorMapper {

    /**
     * Converte um administrador em sua representação de resposta.
     *
     * @param a administrador que será convertido
     * @return DTO com os dados principais do administrador
     */
    public AdministratorResponse toResponse(Administrator a) {
        return new AdministratorResponse(
                a.getId(),
                a.getName(),
                a.getEmail(),
                a.getPhone(),
                a.getAccessLevel(),
                a.isActive()
        );
    }
}