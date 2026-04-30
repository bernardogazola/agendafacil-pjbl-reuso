package br.pucpr.agendafacil.domain.notification;

/**
 * Resultado de uma tentativa de envio de notificação.
 *
 * <p>Este record indica se o envio foi concluído com sucesso, qual mensagem foi
 * formatada e, em caso de falha, qual erro ocorreu.</p>
 *
 * <p>A mensagem formatada é mantida no resultado para que o serviço de aplicação
 * consiga registrar exatamente o conteúdo que tentou enviar, sem precisar
 * montar a mensagem novamente.</p>
 */
public record SendResult(boolean success, String formattedMessage, String errorMessage) {

    /**
     * Cria um resultado de envio bem-sucedido.
     *
     * @param formattedMessage mensagem enviada pelo canal
     * @return resultado de sucesso
     */
    public static SendResult ok(String formattedMessage) {
        return new SendResult(true, formattedMessage, null);
    }

    /**
     * Cria um resultado de falha antes da mensagem ser formatada.
     *
     * @param errorMessage descrição do erro ocorrido
     * @return resultado de falha
     */
    public static SendResult fail(String errorMessage) {
        return new SendResult(false, null, errorMessage);
    }

    /**
     * Cria um resultado de falha após a mensagem ter sido formatada.
     *
     * <p>Esse caso é usado quando o erro acontece durante o despacho, permitindo
     * registrar a mensagem que o sistema tentou enviar.</p>
     *
     * @param formattedMessage mensagem que seria enviada
     * @param errorMessage descrição do erro ocorrido
     * @return resultado de falha com a mensagem preservada
     */
    public static SendResult failAfterFormat(String formattedMessage, String errorMessage) {
        return new SendResult(false, formattedMessage, errorMessage);
    }
}