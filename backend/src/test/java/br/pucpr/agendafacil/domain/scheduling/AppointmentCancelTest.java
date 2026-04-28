package br.pucpr.agendafacil.domain.scheduling;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationResult;
import br.pucpr.agendafacil.domain.scheduling.cancellation.DeadlineCancellation;
import br.pucpr.agendafacil.domain.scheduling.cancellation.FeeBasedCancellation;
import br.pucpr.agendafacil.domain.scheduling.cancellation.FreeCancellation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentCancelTest {

    @Test
    void scheduled_plusAllowed_transitionsToCanceled() {
        Appointment appointment = scheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));

        CancellationResult result = appointment.cancel(
                LocalDateTime.of(2026, 5, 8, 10, 0), new FreeCancellation());

        assertTrue(result.allowed());
        assertEquals(BigDecimal.ZERO, result.fee());
        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());
    }

    @Test
    void confirmed_plusAllowedWithFee_transitionsAndPreservesFee() {
        Appointment appointment = scheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        CancellationResult result = appointment.cancel(
                LocalDateTime.of(2026, 5, 10, 8, 0),
                new FeeBasedCancellation(24, new BigDecimal("0.30")));

        assertTrue(result.allowed());
        assertEquals(new BigDecimal("30.00"), result.fee());
        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());
    }

    @Test
    void scheduled_plusDenied_doesNotMutateStatus() {
        Appointment appointment = scheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));

        CancellationResult result = appointment.cancel(
                LocalDateTime.of(2026, 5, 10, 8, 0), new DeadlineCancellation(24));

        assertEquals(false, result.allowed());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
    }

    @Test
    void completed_plusAllowed_throwsWithPortugueseMessage() {
        Appointment appointment = scheduledAt(LocalDateTime.of(2026, 5, 10, 14, 0));
        appointment.setStatus(AppointmentStatus.COMPLETED);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                appointment.cancel(LocalDateTime.of(2026, 5, 8, 10, 0), new FreeCancellation()));

        assertTrue(ex.getMessage().contains("não pode ser cancelado"),
                "Mensagem deve ser em português; foi: " + ex.getMessage());
    }

    private static Appointment scheduledAt(LocalDateTime at) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.CLINIC, BusinessPlan.PROFESSIONAL, null);
        OfferedService service = new OfferedService("Consulta",
                new BigDecimal("100.00"), 60, business);
        return new Appointment(new Customer(), business, service, at, 60,
                new BigDecimal("100.00"));
    }
}
