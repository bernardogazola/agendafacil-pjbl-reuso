package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Define o fluxo base para processar um agendamento.
 *
 * <p>Esta classe aplica o padrão Template Method: o método {@link #process}
 * fixa a ordem das etapas do processamento, enquanto as subclasses definem os
 * detalhes que variam conforme o tipo de estabelecimento.</p>
 *
 * <p>O fluxo comum valida o horário, aplica regras específicas, bloqueia tempo
 * adicional quando necessário e gera registros complementares quando a categoria
 * exigir.</p>
 */
public abstract class AppointmentProcessor {

    /**
     * Executa o fluxo completo de processamento do agendamento.
     *
     * <p>Este é o template method da classe. Por isso, é {@code final}: as
     * subclasses podem alterar os passos específicos, mas não a ordem geral do
     * processamento.</p>
     */
    public final Appointment process(Appointment requested) {
        validateBaseSchedule(requested);
        applySpecificRules(requested);

        if (shouldBlockAdditionalSlots()) {
            blockAdditionalSlots(requested);
        }

        generateComplementaryRecord(requested);
        return requested;
    }

    /**
     * Valida regras comuns a todos os tipos de agendamento.
     *
     * <p>A implementação padrão rejeita horários no passado e também verifica a
     * antecedência mínima definida pela categoria.</p>
     */
    protected void validateBaseSchedule(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        if (appointment.getScheduledAt().isBefore(now)) {
            throw new IllegalArgumentException("Horário de agendamento está no passado");
        }
        int leadHours = minimumLeadHours();
        if (leadHours > 0) {
            long minutesUntil = Duration.between(now, appointment.getScheduledAt()).toMinutes();
            if (minutesUntil < leadHours * 60L) {
                throw new IllegalArgumentException(
                        "É necessária antecedência mínima de " + leadHours
                                + "h para este tipo de agendamento");
            }
        }
    }

    /**
     * Define a antecedência mínima exigida pela categoria.
     *
     * <p>O valor padrão é {@code 0}, permitindo agendamentos no mesmo dia.
     * Subclasses sobrescrevem este método quando precisam de uma regra mais
     * restritiva, como no caso de clínicas.</p>
     */
    protected int minimumLeadHours() {
        return 0;
    }

    /**
     * Aplica as regras específicas da categoria antes de concluir o processamento.
     */
    protected abstract void applySpecificRules(Appointment appointment);

    /**
     * Indica se o processamento deve bloquear horários adicionais para o agendamento.
     */
    protected abstract boolean shouldBlockAdditionalSlots();

    /**
     * Bloqueia horários adicionais relacionados ao agendamento, quando necessário.
     */
    protected abstract void blockAdditionalSlots(Appointment appointment);

    /**
     * Gera um registro complementar relacionado ao agendamento, quando necessário.
     */
    protected abstract void generateComplementaryRecord(Appointment appointment);
}
