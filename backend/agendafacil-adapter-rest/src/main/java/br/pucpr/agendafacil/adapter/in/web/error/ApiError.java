package br.pucpr.agendafacil.adapter.in.web.error;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Resposta padronizada de erro retornada pela API.
 *
 * <p>Esse payload é usado pelos exception mappers para manter o mesmo formato
 * de resposta em erros de validação, autenticação, autorização, conflito,
 * regra de negócio e falhas inesperadas.</p>
 */
@Schema(description = "Resposta padronizada de erro da API")
public record ApiError(
        @Schema(
                description = "Mensagem descritiva do erro",
                examples = "Agendamento não encontrado"
        )
        String error,

        @Schema(
                description = "Código curto que identifica o tipo do erro",
                examples = "NOT_FOUND"
        )
        String code,

        @Schema(
                description = "Data e hora em que o erro foi registrado",
                format = "date-time",
                examples = "2026-05-06T13:45:30",
                readOnly = true
        )
        LocalDateTime timestamp
) {
    /**
     * Cria uma resposta de erro usando o instante atual do servidor.
     *
     * @param message mensagem descritiva do erro
     * @param code código curto do erro
     * @return erro padronizado da API
     */
    public static ApiError of(String message, String code) {
        return new ApiError(message, code, LocalDateTime.now());
    }
}