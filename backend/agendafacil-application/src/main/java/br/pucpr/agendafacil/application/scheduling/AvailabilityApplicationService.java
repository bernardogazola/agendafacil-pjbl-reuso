package br.pucpr.agendafacil.application.scheduling;

import br.pucpr.agendafacil.application.dto.AvailableSlotsResponse;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessHours;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.port.BusinessHoursRepository;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.Schedule;
import br.pucpr.agendafacil.domain.scheduling.availability.AvailabilityStrategy;
import br.pucpr.agendafacil.domain.scheduling.availability.AvailabilityStrategyFactory;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Serviço de aplicação responsável pela consulta de horários disponíveis.
 *
 * <p>O serviço valida o estabelecimento e o serviço, carrega os horários de
 * funcionamento, busca os agendamentos do dia e usa o agregado
 * {@link Schedule} para calcular os slots disponíveis.</p>
 *
 * <p>A estratégia de disponibilidade é escolhida conforme o estabelecimento,
 * permitindo variar a regra de geração de horários sem alterar este serviço.</p>
 */
@ApplicationScoped
public class AvailabilityApplicationService {

    private final BusinessRepository businessRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityStrategyFactory availabilityStrategyFactory;

    @Inject
    public AvailabilityApplicationService(BusinessRepository businessRepository,
                                          BusinessHoursRepository businessHoursRepository,
                                          OfferedServiceRepository offeredServiceRepository,
                                          AppointmentRepository appointmentRepository,
                                          AvailabilityStrategyFactory availabilityStrategyFactory) {
        this.businessRepository = businessRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityStrategyFactory = availabilityStrategyFactory;
    }

    /**
     * Calcula os horários disponíveis de um serviço em uma data.
     *
     * @param businessId identificador do estabelecimento
     * @param serviceId identificador do serviço
     * @param date data desejada para consulta
     * @return horários disponíveis para agendamento
     */
    public AvailableSlotsResponse getAvailableSlots(Long businessId, Long serviceId, LocalDate date) {
        Business business = businessRepository.findActiveById(businessId)
                .orElseThrow(() -> new NotFoundException("Estabelecimento não encontrado ou inativo."));
        OfferedService service = offeredServiceRepository.findActiveById(serviceId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado ou inativo."));
        if (!service.getBusiness().getId().equals(businessId)) {
            throw new NotFoundException("O serviço não pertence a este estabelecimento.");
        }

        List<BusinessHours> hours = businessHoursRepository.findByBusinessId(businessId);
        business.getBusinessHours().addAll(hours);

        List<Appointment> appointments = appointmentRepository.findByBusinessAndDate(businessId, date);
        Schedule schedule = new Schedule(business, date, date, appointments);
        AvailabilityStrategy strategy = availabilityStrategyFactory.resolve(business);
        List<LocalTime> slots = schedule.availableSlots(date, strategy);

        return new AvailableSlotsResponse(businessId, serviceId, date, slots);
    }
}