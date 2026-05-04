package br.pucpr.agendafacil.domain.scheduling.availability;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityStrategyFactoryTest {

    private final AvailabilityStrategyFactory factory = new AvailabilityStrategyFactory();

    @Test
    void barberShop_usesFixedHours() {
        assertInstanceOf(FixedHoursAvailability.class, factory.resolve(business(BusinessCategory.BARBER_SHOP)));
    }

    @Test
    void clinic_usesSlotBased() {
        assertInstanceOf(SlotBasedAvailability.class, factory.resolve(business(BusinessCategory.CLINIC)));
    }

    @Test
    void personalTrainer_usesOnDemand() {
        assertInstanceOf(OnDemandAvailability.class, factory.resolve(business(BusinessCategory.PERSONAL_TRAINER)));
    }

    @Test
    void other_fallsBackToFixedHours() {
        assertInstanceOf(FixedHoursAvailability.class, factory.resolve(business(BusinessCategory.OTHER)));
    }

    private static Business business(BusinessCategory c) {
        return new Business("Negócio", "abc@a.com", c, BusinessPlan.BASIC, null);
    }
}