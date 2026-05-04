package br.pucpr.agendafacil.shared.exception;

/**
 * Regra de negócio negou a operação.
 */
public class BusinessRuleException extends AgendaException {
    public BusinessRuleException(String message) {
        super(message);
    }
}