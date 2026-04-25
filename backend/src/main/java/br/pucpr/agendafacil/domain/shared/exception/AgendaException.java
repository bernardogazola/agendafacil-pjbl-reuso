package br.pucpr.agendafacil.domain.shared.exception;

/**
 * Exceção-base da aplicação.
 * Todas as exceções de negócio/domínio lançadas pelos services herdam daqui.
 */
public abstract class AgendaException extends RuntimeException {

    protected AgendaException(String message) {
        super(message);
    }
}
