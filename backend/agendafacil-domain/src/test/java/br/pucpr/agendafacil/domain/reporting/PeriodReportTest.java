package br.pucpr.agendafacil.domain.reporting;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodReportTest {

    private static final LocalDate REFERENCE = LocalDate.of(2026, 4, 17); // sexta-feira

    @Test
    void daily_window_isSingleDay() {
        String output = new DailyReport().generate(1L, REFERENCE);

        assertTrue(output.contains("2026-04-17 a 2026-04-17"),
                "janela deve ser o mesmo dia; foi: " + output);
    }

    @Test
    void weekly_window_isMondayThroughSunday() {
        String output = new WeeklyReport().generate(1L, REFERENCE);

        // Semana da sexta 2026-04-17: segunda 2026-04-13 a domingo 2026-04-19
        assertTrue(output.contains("2026-04-13 a 2026-04-19"),
                "janela semanal ISO; foi: " + output);
    }

    @Test
    void monthly_window_isFirstToLastOfMonth() {
        String output = new MonthlyReport().generate(1L, REFERENCE);

        assertTrue(output.contains("2026-04-01 a 2026-04-30"),
                "janela mensal; foi: " + output);
    }

    @Test
    void metrics_aggregateFromFetch() {
        List<Appointment> fixtures = List.of(
                appointmentPaid(new BigDecimal("50.00")),
                appointmentPaid(new BigDecimal("50.00"))
        );
        PeriodReport report = new DailyReport() {
            @Override
            protected List<Appointment> fetch(Long businessId, LocalDate start, LocalDate end) {
                return fixtures;
            }
        };

        String output = report.generate(1L, REFERENCE);

        assertTrue(output.contains("2 atendimentos"),
                "output deveria conter 2 atendimentos; foi: " + output);
        assertTrue(output.contains("100.00"),
                "output deveria conter receita 100.00; foi: " + output);
    }

    @Test
    void format_isPortuguese() {
        String output = new DailyReport().generate(1L, REFERENCE);

        assertTrue(output.startsWith("Relatório de "), "mensagem em PT; foi: " + output);
        assertTrue(output.contains("atendimentos"));
        assertTrue(output.contains("receita"));
    }

    @Test
    void metrics_onEmptyList_areZero() {
        PeriodReport.Metrics metrics = new DailyReport().calculateMetrics(List.of());

        assertEquals(0, metrics.count());
        assertEquals(new BigDecimal("0.00"), metrics.revenue());
    }

    private static Appointment appointmentPaid(BigDecimal price) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        Customer customer = new Customer("Cliente", "c@a.com", "senha");
        OfferedService service = new OfferedService("Serviço",
                price, 30, business);
        return new Appointment(customer, business, service,
                LocalDateTime.of(2026, 4, 17, 10, 0), 30, price);
    }
}
