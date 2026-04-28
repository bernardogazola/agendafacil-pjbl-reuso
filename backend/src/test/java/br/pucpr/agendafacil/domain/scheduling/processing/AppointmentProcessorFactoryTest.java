package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentProcessorFactoryTest {

    private final AppointmentProcessorFactory factory = new AppointmentProcessorFactory();

    @Test
    void barberShop_usesBarberShopProcessor() {
        assertInstanceOf(BarberShopProcessor.class, factory.resolve(business(BusinessCategory.BARBER_SHOP)));
    }

    @Test
    void clinic_usesClinicProcessor() {
        assertInstanceOf(ClinicProcessor.class, factory.resolve(business(BusinessCategory.CLINIC)));
    }

    @Test
    void aesthetics_usesAestheticsProcessor() {
        assertInstanceOf(AestheticsProcessor.class, factory.resolve(business(BusinessCategory.AESTHETICS)));
    }

    @Test
    void psychologist_usesClinicProcessor() {
        assertInstanceOf(ClinicProcessor.class, factory.resolve(business(BusinessCategory.PSYCHOLOGIST)));
    }

    private static Business business(BusinessCategory c) {
        return new Business("Negócio", "abc@a.com", c, BusinessPlan.BASIC, null);
    }
}