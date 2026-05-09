package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.BusinessHoursDTO;
import br.pucpr.agendafacil.application.dto.BusinessHoursResponse;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter horários de funcionamento entre entidade de
 * domínio e DTO.
 */
@ApplicationScoped
public class BusinessHoursMapper {

    /**
     * Converte um horário de funcionamento para DTO.
     *
     * @param bh horário de funcionamento que será convertido
     * @return DTO com os dados do horário
     */
    public BusinessHoursDTO toDto(BusinessHours bh) {
        return new BusinessHoursDTO(
                bh.getDayOfWeek(),
                bh.getStartTime(),
                bh.getEndTime(),
                bh.isActive()
        );
    }

    /**
     * Converte um horário de funcionamento para sua representação de resposta.
     *
     * @param bh horário de funcionamento que será convertido
     * @return DTO com id e dados principais da janela de funcionamento
     */
    public BusinessHoursResponse toResponse(BusinessHours bh) {
        return new BusinessHoursResponse(
                bh.getId(),
                bh.getDayOfWeek(),
                bh.getStartTime(),
                bh.getEndTime(),
                bh.isActive()
        );
    }

    /**
     * Converte um DTO em horário de funcionamento do domínio.
     *
     * @param dto dados do horário de funcionamento
     * @param business estabelecimento dono do horário
     * @return horário de funcionamento criado a partir do DTO
     */
    public BusinessHours toEntity(BusinessHoursDTO dto, Business business) {
        BusinessHours bh = new BusinessHours(
                business,
                dto.dayOfWeek(),
                dto.startTime(),
                dto.endTime()
        );
        if (!dto.active()) bh.deactivate();
        return bh;
    }
}