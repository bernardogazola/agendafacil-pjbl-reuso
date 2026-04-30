package br.pucpr.agendafacil.domain.notification;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.scheduling.Appointment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationSenderTest {

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        System.setOut(new PrintStream(stdout));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void email_withValidRecipient_succeedsAndLogsDispatch() {
        Appointment appointment = appointmentWith(
                customer("Ana", "ana@a.com", "11999990000"));

        SendResult result = new EmailNotificationSender().send(appointment);

        assertTrue(result.success(), "deveria ter sucesso");
        String captured = stdout.toString();
        assertTrue(captured.contains("[EMAIL -> ana@a.com]"),
                "stdout deveria conter o prefixo EMAIL; foi: " + captured);
    }

    @Test
    void email_withInvalidRecipient_failsWithPortugueseReason() {
        Appointment appointment = appointmentWith(
                customer("Ana", "ana_sem_arroba", "11999990000"));

        SendResult result = new EmailNotificationSender().send(appointment);

        assertFalse(result.success());
        assertEquals("Destinatário inválido", result.errorMessage());
    }

    @Test
    void sms_withValidPhone_succeedsAndLogsDispatch() {
        Appointment appointment = appointmentWith(
                customer("Bruno", "bruno@a.com", "11988887777"));

        SendResult result = new SmsNotificationSender().send(appointment);

        assertTrue(result.success());
        assertTrue(stdout.toString().contains("[SMS -> 11988887777]"));
    }

    @Test
    void sms_withBlankPhone_fails() {
        Appointment appointment = appointmentWith(customer("Bruno", "b@a.com", "  "));

        SendResult result = new SmsNotificationSender().send(appointment);

        assertFalse(result.success());
        assertEquals("Destinatário inválido", result.errorMessage());
    }

    @Test
    void whatsapp_happyPath_succeeds() {
        Appointment appointment = appointmentWith(
                customer("Carla", "carla@a.com", "11977776666"));

        SendResult result = new WhatsAppNotificationSender().send(appointment);

        assertTrue(result.success());
        assertTrue(stdout.toString().contains("[WHATSAPP -> 11977776666]"));
    }

    private static Customer customer(String name, String email, String phone) {
        Customer customer = new Customer(name, email, "senha");
        customer.setPhone(phone);
        return customer;
    }

    private static Appointment appointmentWith(Customer customer) {
        Business business = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        OfferedService service = new OfferedService("Corte",
                new BigDecimal("50.00"), 30, business);
        return new Appointment(customer, business, service,
                LocalDateTime.of(2026, 5, 10, 14, 0),
                30, new BigDecimal("50.00"));
    }
}