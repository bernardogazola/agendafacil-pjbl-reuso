package br.pucpr.agendafacil.application.business;

import br.pucpr.agendafacil.application.dto.BusinessHoursDTO;
import br.pucpr.agendafacil.application.dto.CreateOfferedServiceRequest;
import br.pucpr.agendafacil.application.dto.OfferedServiceResponse;
import br.pucpr.agendafacil.application.dto.UpdateBusinessHoursRequest;
import br.pucpr.agendafacil.application.mapper.BusinessHoursMapper;
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
 * Serviço de aplicação responsável pelas operações do dono sobre seus
 * estabelecimentos.
 *
 * <p>Centraliza casos de uso como cadastro de serviços, atualização dos horários
 * de funcionamento e criação dos horários padrão de um estabelecimento recém
 * cadastrado.</p>
 */
@ApplicationScoped
public class BusinessApplicationService {

    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final OfferedServiceMapper offeredServiceMapper;
    private final BusinessHoursMapper businessHoursMapper;

    @Inject
    public BusinessApplicationService(BusinessRepository businessRepository,
                                      OfferedServiceRepository offeredServiceRepository,
                                      BusinessHoursRepository businessHoursRepository,
                                      OfferedServiceMapper offeredServiceMapper,
                                      BusinessHoursMapper businessHoursMapper) {
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.offeredServiceMapper = offeredServiceMapper;
        this.businessHoursMapper = businessHoursMapper;
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