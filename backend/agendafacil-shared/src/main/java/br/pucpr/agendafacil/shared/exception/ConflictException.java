package br.pucpr.agendafacil.shared.exception;

/**
 * Conflito com o estado atual do servidor.
 * E-mail já cadastrado, horário já ocupado, transição de estado inválida.
 * Mapeia para HTTP {@code 409}.
 */
public class ConflictException extends AgendaException {
    public ConflictException(String message) {
        super(message);
    }
}
