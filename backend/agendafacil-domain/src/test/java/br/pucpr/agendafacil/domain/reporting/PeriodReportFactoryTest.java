package br.pucpr.agendafacil.domain.reporting;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodReportFactoryTest {

    private final PeriodReportFactory factory = new PeriodReportFactory();

    private final AppointmentRepository stub = new AppointmentRepository() {
        @Override
        public List<Appointment> findByBusinessAndDateRange(Long businessId,
                                                            LocalDate from, LocalDate to) {
            return List.of();
        }
        @Override public Appointment getById(Long id) { return null; }
        @Override public List<Appointment> findByBusinessAndDate(Long b, LocalDate d) { return List.of(); }
        @Override public List<Appointment> findByCustomerId(Long c) { return List.of(); }
        @Override public List<Appointment> findActiveByCustomerId(Long c) { return List.of(); }
        @Override public Optional<Appointment> findByIdAndCustomerId(Long a, Long c) { return Optional.empty(); }
        @Override public Optional<Appointment> findByIdAndOwnerId(Long appointmentId, Long ownerId) { return Optional.empty(); }
        @Override public List<Appointment> findActiveAfter(LocalDateTime cutoff) { return List.of(); }
        @Override public void persist(Appointment appointment) { /* test stub */ }
        @Override public Appointment update(Appointment appointment) { return null; }
    };

    @Test
    void daily_resolvesDailyReport_viaGenerateFormattedRange() {
        PeriodReport r = factory.resolve(ReportPeriod.DAILY, stub);
        String out = r.generate(1L, LocalDate.of(2026, 4, 17));

        assertTrue(out.contains("2026-04-17 a 2026-04-17"), "janela diária esperada; foi: " + out);
    }

    @Test
    void weekly_resolvesWeeklyReport_viaGenerateFormattedRange() {
        PeriodReport r = factory.resolve(ReportPeriod.WEEKLY, stub);
        String out = r.generate(1L, LocalDate.of(2026, 4, 17)); // sexta

        assertTrue(out.contains("2026-04-13 a 2026-04-19"), "janela semanal ISO; foi: " + out);
    }

    @Test
    void monthly_resolvesMonthlyReport_viaGenerateFormattedRange() {
        PeriodReport r = factory.resolve(ReportPeriod.MONTHLY, stub);
        String out = r.generate(1L, LocalDate.of(2026, 4, 17));

        assertTrue(out.contains("2026-04-01 a 2026-04-30"), "janela mensal; foi: " + out);
    }

    @Test
    void windowStart_andEnd_matchConcreteReports() {
        assertEquals(LocalDate.of(2026, 4, 17),
                factory.windowStart(ReportPeriod.DAILY, LocalDate.of(2026, 4, 17)));
        assertEquals(LocalDate.of(2026, 4, 13),
                factory.windowStart(ReportPeriod.WEEKLY, LocalDate.of(2026, 4, 17)));
        assertEquals(LocalDate.of(2026, 4, 19),
                factory.windowEnd(ReportPeriod.WEEKLY, LocalDate.of(2026, 4, 17)));
        assertEquals(LocalDate.of(2026, 4, 1),
                factory.windowStart(ReportPeriod.MONTHLY, LocalDate.of(2026, 4, 17)));
        assertEquals(LocalDate.of(2026, 4, 30),
                factory.windowEnd(ReportPeriod.MONTHLY, LocalDate.of(2026, 4, 17)));
    }
}
