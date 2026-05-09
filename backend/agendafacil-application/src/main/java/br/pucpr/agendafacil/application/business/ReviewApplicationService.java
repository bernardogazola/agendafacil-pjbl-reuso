package br.pucpr.agendafacil.application.business;

import br.pucpr.agendafacil.application.dto.CreateReviewRequest;
import br.pucpr.agendafacil.application.dto.ReviewResponse;
import br.pucpr.agendafacil.application.dto.UpdateReviewRequest;
import br.pucpr.agendafacil.application.mapper.ReviewMapper;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.Review;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.ReviewRepository;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import br.pucpr.agendafacil.shared.exception.BusinessRuleException;
import br.pucpr.agendafacil.shared.exception.ConflictException;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Serviço de aplicação responsável pela gestão de avaliações.
 *
 * <p>Centraliza a criação, consulta, atualização e remoção de avaliações feitas
 * por clientes após a conclusão de um agendamento.</p>
 *
 * <p>Um cliente só pode avaliar seus próprios agendamentos concluídos. O dono
 * do estabelecimento pode consultar as avaliações relacionadas aos seus
 * agendamentos.</p>
 */
@ApplicationScoped
public class ReviewApplicationService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final ReviewMapper reviewMapper;

    @Inject
    public ReviewApplicationService(ReviewRepository reviewRepository,
                                    AppointmentRepository appointmentRepository,
                                    CustomerRepository customerRepository,
                                    BusinessRepository businessRepository,
                                    ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
        this.reviewMapper = reviewMapper;
    }

    /**
     * Cria uma avaliação para um agendamento concluído do cliente autenticado.
     *
     * <p>O cliente só pode avaliar agendamentos próprios e concluídos. Cada
     * agendamento pode receber apenas uma avaliação.</p>
     *
     * @param customerId identificador do cliente autenticado
     * @param appointmentId identificador do agendamento avaliado
     * @param req dados da avaliação
     * @return avaliação criada
     * @throws BusinessRuleException quando o agendamento ainda não estiver concluído
     * @throws ConflictException quando o agendamento já tiver avaliação
     * @throws NotFoundException quando o agendamento ou cliente não existir
     */
    @Transactional
    public ReviewResponse createReview(Long customerId, Long appointmentId,
                                       CreateReviewRequest req) {
        Appointment appointment = appointmentRepository.getById(appointmentId);
        if (appointment == null
                || appointment.getCustomer() == null
                || !Objects.equals(appointment.getCustomer().getId(), customerId)) {
            throw new NotFoundException("Agendamento não encontrado.");
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Apenas agendamentos concluídos podem ser avaliados.");
        }
        if (reviewRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new ConflictException(
                    "Este agendamento já recebeu uma avaliação.");
        }

        Customer customer = customerRepository.getById(customerId);
        if (customer == null) {
            throw new NotFoundException("Cliente não encontrado.");
        }

        Review review = new Review(appointment, customer, req.rating());
        review.setComment(req.comment());
        reviewRepository.persist(review);
        return reviewMapper.toResponse(review, customer, appointment);
    }

    /**
     * Lista as avaliações feitas pelo cliente autenticado.
     *
     * @param customerId identificador do cliente autenticado
     * @return lista de avaliações do cliente
     */
    public List<ReviewResponse> listMine(Long customerId) {
        return reviewRepository.findByCustomerId(customerId).stream()
                .map(this::hydrate)
                .toList();
    }

    /**
     * Lista as avaliações de um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @return lista de avaliações relacionadas ao estabelecimento
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o estabelecimento não existir
     */
    public List<ReviewResponse> listForBusiness(Long ownerId, Long businessId) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        if (!Objects.equals(business.getOwner().getId(), ownerId)) {
            throw new ForbiddenException(
                    "Você não tem permissão para acessar avaliações deste estabelecimento.");
        }
        return reviewRepository.findByBusinessId(businessId).stream()
                .map(this::hydrate)
                .toList();
    }

    /**
     * Busca uma avaliação feita pelo cliente autenticado.
     *
     * @param customerId identificador do cliente autenticado
     * @param reviewId identificador da avaliação
     * @return dados da avaliação encontrada
     * @throws NotFoundException quando a avaliação não existir ou não pertencer ao cliente
     */
    public ReviewResponse getMine(Long customerId, Long reviewId) {
        Review review = loadOwnedOrThrow(customerId, reviewId);
        return hydrate(review);
    }

    /**
     * Atualiza uma avaliação feita pelo cliente autenticado.
     *
     * @param customerId identificador do cliente autenticado
     * @param reviewId identificador da avaliação
     * @param req novos dados da avaliação
     * @return avaliação atualizada
     * @throws NotFoundException quando a avaliação não existir ou não pertencer ao cliente
     */
    @Transactional
    public ReviewResponse updateMine(Long customerId, Long reviewId,
                                     UpdateReviewRequest req) {
        Review review = loadOwnedOrThrow(customerId, reviewId);
        review.setRating(req.rating());
        review.setComment(req.comment());
        Review saved = reviewRepository.update(review);
        return hydrate(saved);
    }

    /**
     * Remove uma avaliação feita pelo cliente autenticado.
     *
     * @param customerId identificador do cliente autenticado
     * @param reviewId identificador da avaliação
     * @throws NotFoundException quando a avaliação não existir ou não pertencer ao cliente
     */
    @Transactional
    public void deleteMine(Long customerId, Long reviewId) {
        loadOwnedOrThrow(customerId, reviewId);
        reviewRepository.removeById(reviewId);
    }

    private Review loadOwnedOrThrow(Long customerId, Long reviewId) {
        Review review = reviewRepository.getById(reviewId);
        if (review == null
                || review.getCustomer() == null
                || !Objects.equals(review.getCustomer().getId(), customerId)) {
            throw new NotFoundException("Avaliação não encontrada.");
        }
        return review;
    }

    /**
     * Monta a resposta completa de uma avaliação.
     *
     * <p>A avaliação pode vir da persistência apenas com referências parciais.
     * Por isso, o método carrega o cliente e o agendamento antes de chamar o
     * mapper de resposta, permitindo exibir nomes do cliente, estabelecimento e
     * serviço.</p>
     *
     * @param review avaliação que será convertida
     * @return DTO com os dados completos da avaliação
     */
    private ReviewResponse hydrate(Review review) {
        Customer customer = review.getCustomer() != null
                ? customerRepository.getById(review.getCustomer().getId())
                : null;
        Appointment appointment = review.getAppointment() != null
                ? appointmentRepository.getById(review.getAppointment().getId())
                : null;
        return reviewMapper.toResponse(review, customer, appointment);
    }
}