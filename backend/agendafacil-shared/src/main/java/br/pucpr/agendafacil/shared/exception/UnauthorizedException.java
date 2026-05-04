package br.pucpr.agendafacil.shared.exception;

/**
 * Credenciais inválidas ou token ausente.
 * Mapeia para HTTP {@code 401}
 */
public class UnauthorizedException extends AgendaException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

