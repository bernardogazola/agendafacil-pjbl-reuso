package br.pucpr.agendafacil.domain.notification;

/**
 * Canal usado para entregar uma notificação ao cliente.
 *
 * <p>O canal também ajuda a escolher qual implementação de
 * {@link NotificationSender} será usada no envio. Por exemplo, notificações por
 * e-mail, SMS e WhatsApp seguem o mesmo fluxo geral, mas mudam a validação, a
 * formatação da mensagem e a forma de despacho.</p>
 *
 * <p>Os canais também podem ser usados como preferência do cliente, indicando
 * por quais meios ele aceita receber notificações.</p>
 */
public enum NotificationChannel {
    EMAIL,
    SMS,
    WHATSAPP
}