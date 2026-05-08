package br.pucpr.agendafacil.application.business;

import br.pucpr.agendafacil.application.dto.*;
import br.pucpr.agendafacil.application.mapper.BusinessHoursMapper;
import br.pucpr.agendafacil.application.mapper.BusinessMapper;
import br.pucpr.agendafacil.application.mapper.OfferedServiceMapper;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.port.BusinessHoursRepository;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Serviço de aplicação responsável pela gestão de estabelecimentos, serviços e
 * horários de funcionamento.
 *
 * <p>Centraliza casos de uso usados pelo dono do estabelecimento e pela visão
 * administrativa da plataforma, como consulta, atualização, ativação,
 * desativação, cadastro de serviços e edição de horários.</p>
 */
@ApplicationScoped
public class BusinessApplicationService {

    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final BusinessMapper businessMapper;
    private final OfferedServiceMapper offeredServiceMapper;
    private final BusinessHoursMapper businessHoursMapper;

    @Inject
    public BusinessApplicationService(BusinessRepository businessRepository,
                                      OfferedServiceRepository offeredServiceRepository,
                                      BusinessHoursRepository businessHoursRepository,
                                      BusinessMapper businessMapper,
                                      OfferedServiceMapper offeredServiceMapper,
                                      BusinessHoursMapper businessHoursMapper) {
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.businessMapper = businessMapper;
        this.offeredServiceMapper = offeredServiceMapper;
        this.businessHoursMapper = businessHoursMapper;
    }

    /**
     * Busca os dados de um estabelecimento pertencente ao dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @return dados do estabelecimento encontrado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o estabelecimento não existir
     */
    public BusinessResponse getBusiness(Long ownerId, Long businessId) {
        Business business = loadBusinessOrThrow(businessId);
        assertOwnership(ownerId, business);
        return businessMapper.toResponse(business);
    }

    /**
     * Busca os dados de um estabelecimento pela visão administrativa.
     *
     * @param businessId identificador do estabelecimento
     * @return dados do estabelecimento encontrado
     * @throws NotFoundException quando o estabelecimento não existir
     */
    public BusinessResponse getBusinessAdmin(Long businessId) {
        return businessMapper.toResponse(loadBusinessOrThrow(businessId));
    }

    /**
     * Lista os estabelecimentos cadastrados na plataforma.
     *
     * @param activeOnly indica se a listagem deve retornar apenas estabelecimentos ativos
     * @return lista de estabelecimentos encontrados
     */
    public List<BusinessResponse> listAdminAll(boolean activeOnly) {
        return businessRepository.listAllAdmin(activeOnly).stream()
                .map(businessMapper::toResponse)
                .toList();
    }

    /**
     * Atualiza os dados de um estabelecimento pertencente ao dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param req novos dados do estabelecimento
     * @return estabelecimento atualizado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o estabelecimento não existir
     */
    @Transactional
    public BusinessResponse updateBusiness(Long ownerId, Long businessId,
                                           UpdateBusinessRequest req) {
        Business business = loadBusinessOrThrow(businessId);
        assertOwnership(ownerId, business);
        applyBusinessUpdate(business, req);
        Business saved = businessRepository.update(business);
        return businessMapper.toResponse(saved);
    }

    /**
     * Atualiza os dados de um estabelecimento pela visão administrativa.
     *
     * @param businessId identificador do estabelecimento
     * @param req novos dados do estabelecimento
     * @return estabelecimento atualizado
     * @throws NotFoundException quando o estabelecimento não existir
     */
    @Transactional
    public BusinessResponse updateBusinessAdmin(Long businessId, UpdateBusinessRequest req) {
        Business business = loadBusinessOrThrow(businessId);
        applyBusinessUpdate(business, req);
        Business saved = businessRepository.update(business);
        return businessMapper.toResponse(saved);
    }

    /**
     * Desativa um estabelecimento pela visão administrativa.
     *
     * @param businessId identificador do estabelecimento
     * @return estabelecimento desativado
     * @throws NotFoundException quando o estabelecimento não existir
     */
    @Transactional
    public BusinessResponse deactivateBusinessAdmin(Long businessId) {
        Business business = loadBusinessOrThrow(businessId);
        business.deactivate();
        Business saved = businessRepository.update(business);
        return businessMapper.toResponse(saved);
    }

    /**
     * Reativa um estabelecimento pela visão administrativa.
     *
     * @param businessId identificador do estabelecimento
     * @return estabelecimento reativado
     * @throws NotFoundException quando o estabelecimento não existir
     */
    @Transactional
    public BusinessResponse reactivateBusinessAdmin(Long businessId) {
        Business business = loadBusinessOrThrow(businessId);
        business.activate();
        Business saved = businessRepository.update(business);
        return businessMapper.toResponse(saved);
    }

    private void applyBusinessUpdate(Business business, UpdateBusinessRequest req) {
        business.setTradeName(req.tradeName());
        business.setEmail(req.email());
        business.setPhone(req.phone());
        business.setCategory(req.category());
        business.setPlan(req.plan());
        business.setCancellationPolicyType(req.cancellationPolicyType());
    }

    /**
     * Cadastra um novo serviço em um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono do estabelecimento
     * @param businessId identificador do estabelecimento
     * @param req dados do serviço que será cadastrado
     * @return dados do serviço criado
     */
    @Transactional
    public OfferedServiceResponse createOfferedService(Long ownerId, Long businessId,
                                                       CreateOfferedServiceRequest req) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        assertOwnership(ownerId, business);
        OfferedService service = offeredServiceMapper.toEntity(req, business);
        offeredServiceRepository.persist(service);
        return offeredServiceMapper.toResponse(service);
    }

    /**
     * Busca um serviço de um estabelecimento pertencente ao dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return dados do serviço encontrado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o serviço não existir no estabelecimento
     */
    public OfferedServiceResponse getOfferedService(Long ownerId, Long businessId, Long serviceId) {
        OfferedService service = loadServiceOrThrow(serviceId);
        assertServiceOwnership(ownerId, businessId, service);
        return offeredServiceMapper.toResponse(service);
    }

    /**
     * Atualiza um serviço de um estabelecimento pertencente ao dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @param req novos dados do serviço
     * @return serviço atualizado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o serviço não existir no estabelecimento
     */
    @Transactional
    public OfferedServiceResponse updateOfferedService(Long ownerId, Long businessId, Long serviceId,
                                                       UpdateOfferedServiceRequest req) {
        OfferedService service = loadServiceOrThrow(serviceId);
        assertServiceOwnership(ownerId, businessId, service);
        service.setName(req.name());
        service.setBasePrice(req.basePrice());
        service.setDurationMinutes(req.durationMinutes());
        service.setDescription(req.description());
        service.setPricingPolicyType(req.pricingPolicyType());
        OfferedService saved = offeredServiceRepository.update(service);
        return offeredServiceMapper.toResponse(saved);
    }

    /**
     * Desativa um serviço de um estabelecimento pertencente ao dono autenticado.
     *
     * <p>O serviço permanece cadastrado, mas deixa de aparecer na descoberta
     * pública e fica indisponível para novos agendamentos.</p>
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return serviço desativado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o serviço não existir no estabelecimento
     */
    @Transactional
    public OfferedServiceResponse deactivateOfferedService(Long ownerId, Long businessId, Long serviceId) {
        OfferedService service = loadServiceOrThrow(serviceId);
        assertServiceOwnership(ownerId, businessId, service);
        service.deactivate();
        OfferedService saved = offeredServiceRepository.update(service);
        return offeredServiceMapper.toResponse(saved);
    }

    /**
     * Reativa um serviço de um estabelecimento pertencente ao dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @return serviço reativado
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o serviço não existir no estabelecimento
     */
    @Transactional
    public OfferedServiceResponse reactivateOfferedService(Long ownerId, Long businessId, Long serviceId) {
        OfferedService service = loadServiceOrThrow(serviceId);
        assertServiceOwnership(ownerId, businessId, service);
        service.activate();
        OfferedService saved = offeredServiceRepository.update(service);
        return offeredServiceMapper.toResponse(saved);
    }

    private OfferedService loadServiceOrThrow(Long serviceId) {
        OfferedService service = offeredServiceRepository.getById(serviceId);
        if (service == null) {
            throw new NotFoundException("Serviço não encontrado.");
        }
        return service;
    }

    private void assertServiceOwnership(Long ownerId, Long businessId, OfferedService service) {
        if (service.getBusiness() == null
                || !Objects.equals(service.getBusiness().getId(), businessId)) {
            throw new NotFoundException("Serviço não encontrado neste estabelecimento.");
        }
        Business business = loadBusinessOrThrow(businessId);
        assertOwnership(ownerId, business);
    }

    /**
     * Atualiza os horários de funcionamento de um estabelecimento.
     *
     * <p>Para cada dia informado, o método atualiza a janela existente ou cria uma
     * nova quando ainda não houver horário cadastrado para aquele dia.</p>
     *
     * @param ownerId identificador do dono do estabelecimento
     * @param businessId identificador do estabelecimento
     * @param req horários que serão aplicados
     * @return lista atualizada de horários de funcionamento
     */
    @Transactional
    public List<BusinessHoursDTO> updateBusinessHours(Long ownerId, Long businessId,
                                                      UpdateBusinessHoursRequest req) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        assertOwnership(ownerId, business);

        for (BusinessHoursDTO dto : req.hours()) {
            BusinessHours existing = businessHoursRepository
                    .findByBusinessAndDayOfWeek(businessId, dto.dayOfWeek())
                    .orElse(null);
            if (existing == null) {
                businessHoursRepository.persist(businessHoursMapper.toEntity(dto, business));
            } else {
                existing.setStartTime(dto.startTime());
                existing.setEndTime(dto.endTime());
                if (dto.active()) existing.activate();
                else existing.deactivate();
            }
        }

        return businessHoursRepository.findByBusinessId(businessId).stream()
                .map(businessHoursMapper::toDto)
                .toList();
    }

    /**
     * Cria os horários padrão de funcionamento para um estabelecimento novo.
     *
     * <p>Por padrão, segunda a sexta ficam ativos das 09:00 às 18:00. Sábado e
     * domingo são criados das 09:00 às 12:00, mas ficam inativos.</p>
     *
     * @param business estabelecimento que receberá os horários padrão
     */
    @Transactional
    public void initializeDefaultHours(Business business) {
        LocalTime open = LocalTime.of(9, 0);
        LocalTime close = LocalTime.of(18, 0);
        LocalTime weekendOpen = LocalTime.of(9, 0);
        LocalTime weekendClose = LocalTime.of(12, 0);
        for (DayOfWeek day : DayOfWeek.values()) {
            BusinessHours bh = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
                    ? new BusinessHours(business, day, weekendOpen, weekendClose)
                    : new BusinessHours(business, day, open, close);
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) bh.deactivate();
            businessHoursRepository.persist(bh);
        }
    }

    private Business loadBusinessOrThrow(Long businessId) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        return business;
    }

    /**
     * Verifica se o usuário informado é o dono do estabelecimento.
     *
     * @param ownerId identificador do usuário autenticado
     * @param business estabelecimento que será validado
     * @throws ForbiddenException quando o estabelecimento não pertence ao usuário
     */
    public void assertOwnership(Long ownerId, Business business) {
        if (!Objects.equals(business.getOwner().getId(), ownerId)) {
            throw new ForbiddenException("Você não tem permissão para alterar este estabelecimento.");
        }
    }
}