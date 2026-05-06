package br.pucpr.agendafacil.application.reporting;

import br.pucpr.agendafacil.application.dto.ReportResponse;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.reporting.PeriodReport;
import br.pucpr.agendafacil.domain.reporting.PeriodReportFactory;
import br.pucpr.agendafacil.domain.reporting.ReportPeriod;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Serviço de aplicação responsável pela geração de relatórios de agendamentos.
 *
 * <p>O serviço valida se o dono tem acesso ao estabelecimento e delega a geração
 * do texto do relatório para {@link PeriodReport}, escolhido pela
 * {@link PeriodReportFactory} conforme o período solicitado.</p>
 */
@ApplicationScoped
public class ReportApplicationService {

    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final PeriodReportFactory periodReportFactory;

    @Inject
    public ReportApplicationService(BusinessRepository businessRepository,
                                    AppointmentRepository appointmentRepository,
                                    PeriodReportFactory periodReportFactory) {
        this.businessRepository = businessRepository;
        this.appointmentRepository = appointmentRepository;
        this.periodReportFactory = periodReportFactory;
    }

    /**
     * Gera um relatório de agendamentos para um estabelecimento.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param period período desejado para o relatório
     * @param reference data usada como referência para calcular a janela
     * @return relatório com período, quantidade de atendimentos, receita e texto formatado
     */
    public ReportResponse generateReport(Long ownerId, Long businessId,
                                         ReportPeriod period, LocalDate reference) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        if (!Objects.equals(business.getOwner().getId(), ownerId)) {
            throw new ForbiddenException(
                    "Você não tem permissão para consultar os relatórios deste estabelecimento.");
        }

        PeriodReport report = periodReportFactory.resolve(period, appointmentRepository);
        String formatted = report.generate(businessId, reference);

        LocalDate start = periodReportFactory.windowStart(period, reference);
        LocalDate end = periodReportFactory.windowEnd(period, reference);
        List<Appointment> appointments =
                appointmentRepository.findByBusinessAndDateRange(businessId, start, end);
        BigDecimal revenue = appointments.stream()
                .map(Appointment::getPricePaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new ReportResponse(period, start, end,
                appointments.size(), revenue, formatted);
    }
}