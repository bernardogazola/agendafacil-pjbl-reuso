package br.pucpr.agendafacil.domain.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationSenderFactoryTest {

    private final NotificationSenderFactory factory = new NotificationSenderFactory();

    @Test
    void email_resolvesToEmailSender() {
        assertInstanceOf(EmailNotificationSender.class, factory.resolve(NotificationChannel.EMAIL));
    }

    @Test
    void sms_resolvesToSmsSender() {
        assertInstanceOf(SmsNotificationSender.class, factory.resolve(NotificationChannel.SMS));
    }

    @Test
    void whatsapp_resolvesToWhatsAppSender() {
        assertInstanceOf(WhatsAppNotificationSender.class, factory.resolve(NotificationChannel.WHATSAPP));
    }
}