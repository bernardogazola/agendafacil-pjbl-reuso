package br.pucpr.agendafacil.domain.shared.exception;

/**
 * Recurso solicitado não existe ou está desativado.
 * Mapeia para HTTP {@code 404}.
 */
public class NotFoundException extends AgendaException {
    public NotFoundException(String message) {
        super(message);
    }
}

