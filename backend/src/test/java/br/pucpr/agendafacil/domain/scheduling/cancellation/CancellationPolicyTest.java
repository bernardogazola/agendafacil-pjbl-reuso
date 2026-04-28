package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CancellationPolicyTest {

    @Test
    void freeCancellation_alwaysAllowed() {
        Appointment appointment = appointmentAt(LocalDateTime.of(2026, 5, 10, 14, 0),
                new BigDecimal("100.00"));
        CancellationResult result = new FreeCancellation()
                .evaluate(appointment, LocalDateTime.of(2026, 5, 10, 13, 30));

        assertTrue(result.allowed());
        assertEquals(BigDecimal.ZERO, result.fee());
    }

    @Test
    void deadlineCancellation_allowedWhenAboveWindow() {
        Appointment appointment = appointmentAt(LocalDateTime.of(2026, 5, 10, 14, 0),
                new BigDecimal("100.00"));
        CancellationResult result = new DeadlineCancellation(24)
                .evaluate(appointment, LocalDateTime.of(2026, 5, 8, 14, 0));

        assertTrue(result.allowed());
    }

    @Test
    void deadlineCancellation_deniedWithPortugueseReasonWhenBelowWindow() {
        Appointment appointment = appointmentAt(LocalDateTime.of(2026, 5, 10, 14, 0),
                new BigDecimal("100.00"));
        CancellationResult result = new DeadlineCancellation(24)
                .evaluate(appointment, LocalDateTime.of(2026, 5, 10, 8, 0));

        assertFalse(result.allowed());
        assertTrue(result.reason().contains("antecedência"),
                "Mensagem deve ser em português; foi: " + result.reason());
        assertTrue(result.reason().contains("24"));
    }

    @Test
    void feeBasedCancellation_allowedWithFeeWhenBelowWindow() {
        Appointment appointment = appointmentAt(LocalDateTime.of(2026, 5, 10, 14, 0),
                new BigDecimal("100.00"));
        CancellationResult result = new FeeBasedCancellation(24, new BigDecimal("0.30"))
                .evaluate(appointment, LocalDateTime.of(2026, 5, 10, 8, 0));

        assertTrue(result.allowed());
        assertEquals(new BigDecimal("30.00"), result.fee());
    }

    @Test
    void feeBasedCancellation_allowedWithoutFeeWhenAboveWindow() {
        Appointment appointment = appointmentAt(LocalDateTime.of(2026, 5, 10, 14, 0),
                new BigDecimal("100.00"));
        CancellationResult result = new FeeBasedCancellation(24, new BigDecimal("0.30"))
                .evaluate(appointment, LocalDateTime.of(2026, 5, 8, 14, 0));

        assertTrue(result.allowed());
        assertEquals(BigDecimal.ZERO, result.fee());
    }

    private static Appointment appointmentAt(LocalDateTime at, BigDecimal pricePaid) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.CLINIC, BusinessPlan.PROFESSIONAL, null);
        OfferedService service = new OfferedService("Consulta",
                new BigDecimal("100.00"), 60, business);
        return new Appointment(new Customer(), business, service, at, 60, pricePaid);
    }
}