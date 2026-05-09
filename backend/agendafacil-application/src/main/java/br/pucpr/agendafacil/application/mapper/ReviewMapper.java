package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.ReviewResponse;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.Review;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter avaliações para DTOs de resposta.
 *
 * <p>A avaliação pode chegar da persistência apenas com referências parciais,
 * por isso o cliente e o agendamento são recebidos separadamente quando a
 * resposta precisa exibir nomes do cliente, estabelecimento e serviço.</p>
 */
@ApplicationScoped
public class ReviewMapper {

    /**
     * Converte uma avaliação em sua representação de resposta.
     *
     * <p>Além dos dados da avaliação, o método inclui informações do cliente,
     * estabelecimento e serviço quando esses objetos forem informados pelo
     * chamador.</p>
     *
     * @param review avaliação que será convertida
     * @param customer cliente que fez a avaliação
     * @param appointment agendamento avaliado
     * @return DTO com os dados da avaliação
     */
    public ReviewResponse toResponse(Review review,
                                     Customer customer,
                                     Appointment appointment) {
        String customerName = customer != null ? customer.getName() : null;
        Long appointmentId = review.getAppointment() != null
                ? review.getAppointment().getId()
                : null;
        Long customerId = customer != null ? customer.getId() : null;

        Long businessId = null;
        String businessName = null;
        Long serviceId = null;
        String serviceName = null;

        if (appointment != null) {
            Business business = appointment.getBusiness();
            if (business != null) {
                businessId = business.getId();
                businessName = business.getTradeName();
            }

            OfferedService service = appointment.getOfferedService();
            if (service != null) {
                serviceId = service.getId();
                serviceName = service.getName();
            }
        }

        return new ReviewResponse(
                review.getId(),
                appointmentId,
                customerId,
                customerName,
                businessId,
                businessName,
                serviceId,
                serviceName,
                review.getRating(),
                review.getComment()
        );
    }
}