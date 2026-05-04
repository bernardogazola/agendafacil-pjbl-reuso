package br.pucpr.agendafacil.domain.scheduling.processing;

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

class AppointmentProcessorTest {

    @Test
    void barberShop_happyPath_keepsDuration() {
        Appointment appointment = futureAppointment(60);

        Appointment result = new BarberShopProcessor().process(appointment);

        assertSame(appointment, result);
        assertEquals(60, result.getEstimatedDurationMinutes());
        assertNull(result.getClinicalRecordRef(), "Barbearia não gera ficha clínica");
    }

    @Test
    void clinic_rejectsLeadTimeBelow2Hours() {
        Appointment soon = appointmentAt(LocalDateTime.now().plusMinutes(30), 60);
        soon.setNotes("Dor lombar");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ClinicProcessor().process(soon));

        assertTrue(ex.getMessage().toLowerCase().contains("antecedência"),
                "Mensagem deve indicar falta de antecedência; foi: " + ex.getMessage());
    }

    @Test
    void clinic_rejectsBlankNotes() {
        Appointment appointment = futureAppointment(60);
        appointment.setNotes(null);

        IllegalArgumentException nullCase = assertThrows(IllegalArgumentException.class,
                () -> new ClinicProcessor().process(appointment));
        assertTrue(nullCase.getMessage().toLowerCase().contains("motivo"),
                "Mensagem deve mencionar motivo da consulta; foi: " + nullCase.getMessage());

        Appointment blank = futureAppointment(60);
        blank.setNotes("   ");
        IllegalArgumentException blankCase = assertThrows(IllegalArgumentException.class,
                () -> new ClinicProcessor().process(blank));
        assertTrue(blankCase.getMessage().toLowerCase().contains("motivo"));
    }

    @Test
    void clinic_happyPath_setsClinicalRecordRef() {
        Appointment appointment = futureAppointment(60);
        appointment.setNotes("Anamnese: dor lombar há 3 dias");

        Appointment result = new ClinicProcessor().process(appointment);

        String ref = result.getClinicalRecordRef();
        assertNotNull(ref, "ClinicProcessor deve gerar referência de ficha clínica");
        assertTrue(ref.startsWith("FICHA-"),
                "Referência deve começar com FICHA-; foi: " + ref);
        assertTrue(ref.endsWith("-NEW"),
                "Customer sem id deve receber sufixo NEW; foi: " + ref);
    }

    @Test
    void clinic_generateComplementaryRecord_isIdempotent() {
        Appointment appointment = futureAppointment(60);
        appointment.setNotes("Consulta de rotina");

        ClinicProcessor processor = new ClinicProcessor();
        processor.process(appointment);
        String firstRef = appointment.getClinicalRecordRef();
        processor.process(appointment);
        String secondRef = appointment.getClinicalRecordRef();

        assertEquals(firstRef, secondRef,
                "Segunda chamada não deve sobrescrever a referência já gerada");
    }

    @Test
    void aesthetics_extendsDurationByBuffer() {
        Appointment appointment = futureAppointment(60);

        Appointment result = new AestheticsProcessor().process(appointment);

        assertEquals(75, result.getEstimatedDurationMinutes(),
                "Estética soma buffer de 15 min à duração do serviço");
    }

    @Test
    void aesthetics_rejectsServicesShorterThan20Min() {
        Appointment appointment = futureAppointment(15);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AestheticsProcessor().process(appointment));

        assertTrue(ex.getMessage().toLowerCase().contains("duração"),
                "Mensagem deve mencionar duração mínima; foi: " + ex.getMessage());
    }

    @Test
    void aesthetics_blockAdditionalSlots_isIdempotent() {
        Appointment appointment = futureAppointment(60);

        AestheticsProcessor processor = new AestheticsProcessor();
        processor.process(appointment);
        int afterFirst = appointment.getEstimatedDurationMinutes();
        processor.process(appointment);
        int afterSecond = appointment.getEstimatedDurationMinutes();

        assertEquals(afterFirst, afterSecond,
                "Buffer não deve ser somado duas vezes em chamadas reentrantes");
    }

    @Test
    void pastAppointment_throwsWithPortugueseMessage() {
        Appointment past = appointmentAt(LocalDateTime.now().minusDays(1), 60);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new BarberShopProcessor().process(past));

        assertTrue(ex.getMessage().contains("passado"),
                "Mensagem deve ser em português; foi: " + ex.getMessage());
    }

    private static Appointment futureAppointment(int durationMinutes) {
        return appointmentAt(LocalDateTime.now().plusDays(7), durationMinutes);
    }

    private static Appointment appointmentAt(LocalDateTime at, int durationMinutes) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.CLINIC, BusinessPlan.BASIC, null);
        Customer customer = new Customer("Ana", "ana@a.com", "senha");
        OfferedService service = new OfferedService("Serviço",
                new BigDecimal("100.00"), durationMinutes, business);
        return new Appointment(customer, business, service, at, durationMinutes,
                new BigDecimal("100.00"));
    }
}