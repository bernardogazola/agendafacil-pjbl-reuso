package br.pucpr.agendafacil.domain.scheduling.pricing;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingPolicyFactoryTest {

    private final PricingPolicyFactory factory = new PricingPolicyFactory();

    @Test
    void fixed_resolvesToFixedPrice() {
        OfferedService s = service(PricingPolicyType.FIXED);
        assertInstanceOf(FixedPrice.class, factory.resolve(s));
    }

    @Test
    void durationBased_resolvesToDurationBasedPrice() {
        OfferedService s = service(PricingPolicyType.DURATION_BASED);
        assertInstanceOf(DurationBasedPrice.class, factory.resolve(s));
    }

    @Test
    void discounted_resolvesToDiscountedPrice() {
        OfferedService s = service(PricingPolicyType.DISCOUNTED);
        assertInstanceOf(DiscountedPrice.class, factory.resolve(s));
    }

    private static OfferedService service(PricingPolicyType type) {
        Business b = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        OfferedService s = new OfferedService("Corte", new BigDecimal("50.00"), 30, b);
        s.setPricingPolicyType(type);
        return s;
    }
}