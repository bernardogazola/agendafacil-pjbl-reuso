package br.pucpr.agendafacil.domain.scheduling.pricing;

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

class PricingPolicyTest {

    @Test
    void fixedPrice_returnsBasePrice() {
        OfferedService service = service(new BigDecimal("80.00"), 30);
        Appointment appointment = appointmentFor(service);

        double price = new FixedPrice().calculate(service, appointment);

        assertEquals(80.00, price, 0.001);
    }

    @Test
    void durationBasedPrice_multipliesRatePerMinuteByDuration() {
        OfferedService service = service(new BigDecimal("0.00"), 60);
        Appointment appointment = appointmentFor(service);

        double price = new DurationBasedPrice(3.0).calculate(service, appointment);

        assertEquals(180.00, price, 0.001);
    }

    @Test
    void discountedPrice_composesOverAnotherPolicy() {
        OfferedService service = service(new BigDecimal("0.00"), 60);
        Appointment appointment = appointmentFor(service);

        PricingPolicy composed = new DiscountedPrice(new DurationBasedPrice(3.0), 0.20);
        double price = composed.calculate(service, appointment);

        assertEquals(144.00, price, 0.001);
    }

    @Test
    void context_onOfferedService_roundsToTwoDecimalsHalfUp() {
        OfferedService service = service(new BigDecimal("33.333"), 60);
        Appointment appointment = appointmentFor(service);

        BigDecimal result = service.calculateFinalPrice(appointment, new FixedPrice());

        assertEquals(new BigDecimal("33.33"), result);
    }

    @Test
    void context_onOfferedService_handlesDiscountedDurationBased() {
        OfferedService service = service(new BigDecimal("0.00"), 45);
        Appointment appointment = appointmentFor(service);
        PricingPolicy policy = new DiscountedPrice(new DurationBasedPrice(4.0), 0.10);

        BigDecimal result = service.calculateFinalPrice(appointment, policy);

        assertEquals(new BigDecimal("162.00"), result);
    }

    private static OfferedService service(BigDecimal basePrice, int durationMinutes) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        return new OfferedService("Serviço", basePrice, durationMinutes, business);
    }

    private static Appointment appointmentFor(OfferedService service) {
        return new Appointment(new Customer(), service.getBusiness(), service,
                LocalDateTime.of(2026, 5, 1, 10, 0),
                service.getDurationMinutes(), service.getBasePrice());
    }
}