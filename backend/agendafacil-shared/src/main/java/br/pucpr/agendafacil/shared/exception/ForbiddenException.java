package br.pucpr.agendafacil.shared.exception;

/**
 * Usuário autenticado tentou acessar recurso que não pertence a ele.
 * Mapeia para HTTP {@code 403}.
 */
public class ForbiddenException extends AgendaException {
    public ForbiddenException(String message) {
        super(message);
    }
}

