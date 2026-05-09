package br.pucpr.agendafacil.application.scheduling;

import br.pucpr.agendafacil.application.dto.*;
import br.pucpr.agendafacil.application.mapper.AppointmentMapper;
import br.pucpr.agendafacil.application.notification.NotificationApplicationService;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import br.pucpr.agendafacil.domain.scheduling.Schedule;
import br.pucpr.agendafacil.domain.scheduling.cache.AppointmentRegistry;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicy;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyFactory;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationResult;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicy;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyFactory;
import br.pucpr.agendafacil.domain.scheduling.processing.AppointmentProcessor;
import br.pucpr.agendafacil.domain.scheduling.processing.AppointmentProcessorFactory;
import br.pucpr.agendafacil.shared.exception.BusinessRuleException;
import br.pucpr.agendafacil.shared.exception.ConflictException;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Serviço de aplicação responsável pelos casos de uso de agendamento.
 *
 * <p>Centraliza a criação, cancelamento, consulta, alteração de status e
 * reagendamento de agendamentos. Durante a reserva, o serviço valida cliente,
 * estabelecimento e serviço, aplica as regras específicas da categoria,
 * verifica conflitos de agenda, calcula o preço final e registra a reserva.</p>
 *
 * <p>Também aciona os componentes de domínio usados no fluxo, como
 * processadores de agendamento, políticas de preço, políticas de cancelamento,
 * registro de conflitos e envio de notificações.</p>
 */
@ApplicationScoped
public class AppointmentApplicationService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final AppointmentMapper appointmentMapper;

    private final AppointmentProcessorFactory processorFactory;
    private final PricingPolicyFactory pricingPolicyFactory;
    private final CancellationPolicyFactory cancellationPolicyFactory;

    private final NotificationApplicationService notificationService;

    @Inject
    public AppointmentApplicationService(AppointmentRepository appointmentRepository,
                                         CustomerRepository customerRepository,
                                         BusinessRepository businessRepository,
                                         OfferedServiceRepository offeredServiceRepository,
                                         AppointmentMapper appointmentMapper,
                                         AppointmentProcessorFactory processorFactory,
                                         PricingPolicyFactory pricingPolicyFactory,
                                         CancellationPolicyFactory cancellationPolicyFactory,
                                         NotificationApplicationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.appointmentMapper = appointmentMapper;
        this.processorFactory = processorFactory;
        this.pricingPolicyFactory = pricingPolicyFactory;
        this.cancellationPolicyFactory = cancellationPolicyFactory;
        this.notificationService = notificationService;
    }

    /**
     * Realiza a reserva de um agendamento para o cliente autenticado.
     *
     * <p>O fluxo valida os dados da reserva, aplica as regras específicas do tipo
     * de estabelecimento, verifica conflito de horário, calcula o preço final,
     * persiste o agendamento e dispara a notificação de confirmação.</p>
     *
     * @param customerId identificador do cliente autenticado
     * @param req dados da reserva solicitada
     * @return dados do agendamento criado
     */
    @Transactional
    public AppointmentResponse bookAppointment(Long customerId, BookAppointmentRequest req) {
        Customer customer = customerRepository.getById(customerId);
        if (customer == null) {
            throw new NotFoundException("Cliente não encontrado.");
        }
        Business business = businessRepository.findActiveById(req.businessId())
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado ou inativo."));
        OfferedService service = offeredServiceRepository.findActiveById(req.serviceId())
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado ou inativo."));
        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new NotFoundException("O serviço não pertence a este estabelecimento.");
        }

        Appointment candidate = new Appointment(customer, business, service,
                req.scheduledAt(), service.getDurationMinutes(), BigDecimal.ZERO);
        candidate.setNotes(req.notes());

        // Aplica o fluxo específico da categoria do estabelecimento
        // Esse processamento pode validar pré-condições e ajustar a duração efetiva
        AppointmentProcessor processor = processorFactory.resolve(business);
        Appointment processed;
        try {
            processed = processor.process(candidate);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(e.getMessage());
        }

        // A verificação de conflito usa a duração já processada,
        // incluindo possíveis buffers adicionados pelo processador
        List<Appointment> sameDay = appointmentRepository
                .findByBusinessAndDate(business.getId(), processed.getScheduledAt().toLocalDate());
        Schedule schedule = new Schedule(business,
                processed.getScheduledAt().toLocalDate(),
                processed.getScheduledAt().toLocalDate(), sameDay);
        if (schedule.hasConflict(processed.getScheduledAt(), processed.getEstimatedDurationMinutes())) {
            throw new ConflictException("Horário indisponível para este estabelecimento.");
        }

        // Calcula o preço final conforme a política configurada para o serviço
        PricingPolicy pricing = pricingPolicyFactory.resolve(service);
        processed.setPricePaid(service.calculateFinalPrice(processed, pricing));

        appointmentRepository.persist(processed);

        // Atualiza o registro em memória usado nas verificações de conflito
        AppointmentRegistry.getInstance().register(processed);

        // Envia a confirmação como melhor esforço, sem bloquear a reserva
        notificationService.sendBookingConfirmation(processed);

        return appointmentMapper.toResponse(processed);
    }

    /**
     * Cancela um agendamento do cliente autenticado.
     *
     * <p>O cancelamento é avaliado pela política configurada para o estabelecimento.
     * Quando permitido, o agendamento é atualizado, removido do registro de
     * conflitos e uma notificação de cancelamento é enviada.</p>
     *
     * @param appointmentId identificador do agendamento
     * @param customerId identificador do cliente autenticado
     * @return resultado do cancelamento
     */
    @Transactional
    public CancellationResponse cancelAppointment(Long appointmentId, Long customerId) {
        Appointment appt = appointmentRepository
                .findByIdAndCustomerId(appointmentId, customerId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        // A política de cancelamento define se o cliente ainda pode cancelar
        // e se existe alguma taxa associada
        CancellationPolicy policy = cancellationPolicyFactory.resolve(appt.getBusiness());
        CancellationResult result = appt.cancel(LocalDateTime.now(), policy);

        if (!result.allowed()) {
            throw new BusinessRuleException(result.reason());
        }

        appointmentRepository.persist(appt);
        AppointmentRegistry.getInstance().unregister(appt);
        notificationService.sendCancellation(appt);

        BigDecimal fee = result.fee() == null ? BigDecimal.ZERO : result.fee();
        return new CancellationResponse(true, appt.getStatus(), fee, result.reason());
    }

    /**
     * Lista os agendamentos de um estabelecimento dentro de um período.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param from data inicial da consulta
     * @param to data final da consulta
     * @return lista de agendamentos encontrados
     */
    public List<AppointmentResponse> listForBusiness(Long ownerId, Long businessId,
                                                     LocalDate from, LocalDate to) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        if (!Objects.equals(business.getOwner().getId(), ownerId)) {
            throw new ForbiddenException("Você não tem permissão para consultar estes agendamentos.");
        }
        return appointmentRepository.findByBusinessAndDateRange(businessId, from, to).stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    /**
     * Lista os agendamentos do cliente autenticado.
     *
     * @param customerId identificador do cliente
     * @param includeAll indica se deve incluir também agendamentos não ativos
     * @return lista de agendamentos do cliente
     */
    public List<AppointmentResponse> listForCustomer(Long customerId, boolean includeAll) {
        List<Appointment> list = includeAll
                ? appointmentRepository.findByCustomerId(customerId)
                : appointmentRepository.findActiveByCustomerId(customerId);
        return list.stream().map(appointmentMapper::toResponse).toList();
    }

    /**
     * Altera o status de um agendamento a partir de uma ação solicitada pelo dono
     * do estabelecimento.
     *
     * <p>A ação recebida é convertida para o status de destino e a transição é
     * validada pela máquina de estados de {@link AppointmentStatus}. Transições
     * não permitidas são rejeitadas como regra de negócio.</p>
     *
     * @param appointmentId identificador do agendamento
     * @param ownerId identificador do dono autenticado
     * @param req ação de mudança de status
     * @return agendamento atualizado
     * @throws NotFoundException quando o agendamento não existir ou não pertencer ao dono
     * @throws BusinessRuleException quando a transição de status não for permitida
     */
    @Transactional
    public AppointmentResponse changeStatus(Long appointmentId, Long ownerId,
                                            UpdateAppointmentStatusRequest req) {
        Appointment appt = appointmentRepository
                .findByIdAndOwnerId(appointmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        AppointmentStatus target = mapAction(req.action());
        if (!appt.getStatus().canTransitionTo(target)) {
            throw new BusinessRuleException(
                    "Transição inválida: " + appt.getStatus() + " → " + target + ".");
        }
        appt.setStatus(target);

        Appointment saved = appointmentRepository.update(appt);

        if (target == AppointmentStatus.COMPLETED
                || target == AppointmentStatus.NO_SHOW
                || target == AppointmentStatus.CANCELED) {
            AppointmentRegistry.getInstance().unregister(saved);
        }
        return appointmentMapper.toResponse(saved);
    }

    /**
     * Reagenda um agendamento ativo pertencente ao dono do estabelecimento.
     *
     * <p>Antes de salvar o novo horário, o método verifica se existe conflito na
     * agenda do estabelecimento. Caso o novo horário esteja indisponível, o
     * agendamento mantém o horário anterior.</p>
     *
     * @param appointmentId identificador do agendamento
     * @param ownerId identificador do dono autenticado
     * @param req nova data e hora do agendamento
     * @return agendamento reagendado
     * @throws NotFoundException quando o agendamento não existir ou não pertencer ao dono
     * @throws BusinessRuleException quando o agendamento não estiver ativo
     * @throws ConflictException quando o novo horário estiver indisponível
     */
    @Transactional
    public AppointmentResponse reschedule(Long appointmentId, Long ownerId,
                                          RescheduleAppointmentRequest req) {
        Appointment appt = appointmentRepository
                .findByIdAndOwnerId(appointmentId, ownerId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        if (!appt.isActive()) {
            throw new BusinessRuleException(
                    "Apenas agendamentos ativos podem ser reagendados.");
        }

        LocalDateTime previousAt = appt.getScheduledAt();
        AppointmentRegistry.getInstance().unregister(appt);

        try {
            appt.setScheduledAt(req.newScheduledAt());

            List<Appointment> sameDay = appointmentRepository
                    .findByBusinessAndDate(appt.getBusiness().getId(),
                            req.newScheduledAt().toLocalDate())
                    .stream()
                    .filter(a -> !Objects.equals(a.getId(), appt.getId()))
                    .toList();

            Schedule schedule = new Schedule(
                    appt.getBusiness(),
                    req.newScheduledAt().toLocalDate(),
                    req.newScheduledAt().toLocalDate(),
                    sameDay
            );

            if (schedule.hasConflict(req.newScheduledAt(), appt.getEstimatedDurationMinutes())) {
                throw new ConflictException(
                        "Horário indisponível para este estabelecimento.");
            }

            Appointment saved = appointmentRepository.update(appt);
            AppointmentRegistry.getInstance().register(saved);

            return appointmentMapper.toResponse(saved);
        } catch (RuntimeException e) {
            appt.setScheduledAt(previousAt);
            AppointmentRegistry.getInstance().register(appt);
            throw e;
        }
    }

    private AppointmentStatus mapAction(AppointmentStatusAction action) {
        return switch (action) {
            case CONFIRM -> AppointmentStatus.CONFIRMED;
            case COMPLETE -> AppointmentStatus.COMPLETED;
            case MARK_NO_SHOW -> AppointmentStatus.NO_SHOW;
        };
    }

    /**
     * Retorna o status de um agendamento.
     *
     * <p>Método usado apenas por testes.</p>
     */
    AppointmentStatus statusOf(Long appointmentId) {
        Appointment a = appointmentRepository.getById(appointmentId);
        return a == null ? null : a.getStatus();
    }
}