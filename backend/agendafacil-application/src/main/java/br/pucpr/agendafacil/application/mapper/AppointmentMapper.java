package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.AppointmentResponse;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter agendamentos para DTOs de resposta.
 */
@ApplicationScoped
public class AppointmentMapper {

    /**
     * Converte um agendamento em sua representação de resposta.
     *
     * @param a agendamento que será convertido
     * @return DTO com os dados principais do agendamento
     */
    public AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getBusiness() == null ? null : a.getBusiness().getId(),
                a.getBusiness() == null ? null : a.getBusiness().getTradeName(),
                a.getOfferedService() == null ? null : a.getOfferedService().getId(),
                a.getOfferedService() == null ? null : a.getOfferedService().getName(),
                a.getCustomer() == null ? null : a.getCustomer().getId(),
                a.getCustomer() == null ? null : a.getCustomer().getName(),
                a.getScheduledAt(),
                a.getEstimatedDurationMinutes(),
                a.getPricePaid(),
                a.getStatus(),
                a.getNotes(),
                a.getClinicalRecordRef()
        );
    }
}