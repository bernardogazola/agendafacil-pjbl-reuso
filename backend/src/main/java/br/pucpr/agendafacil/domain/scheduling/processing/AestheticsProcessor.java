package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Processador de agendamentos para serviços de estética.
 *
 * <p>Além do fluxo comum definido por {@link AppointmentProcessor}, este
 * processador bloqueia um intervalo adicional após o atendimento. Esse tempo
 * pode representar preparação da sala, limpeza ou intervalo entre
 * procedimentos.</p>
 */
public class AestheticsProcessor extends AppointmentProcessor {

    private static final int BUFFER_AFTER_MINUTES = 15;
    private static final int MIN_PROCEDURE_MINUTES = 20;

    @Override
    protected void applySpecificRules(Appointment appointment) {
        OfferedService service = appointment.getOfferedService();
        Integer baseDuration = service == null ? null : service.getDurationMinutes();
        if (baseDuration == null || baseDuration < MIN_PROCEDURE_MINUTES) {
            throw new IllegalArgumentException(
                    "Procedimentos estéticos exigem duração mínima de "
                            + MIN_PROCEDURE_MINUTES + " minutos");
        }
    }

    @Override
    protected boolean shouldBlockAdditionalSlots() {
        return true;
    }

    @Override
    protected void blockAdditionalSlots(Appointment appointment) {
        // A duração efetiva do agendamento inclui o tempo de atendimento
        // mais o intervalo necessário após o procedimento.
        int base = resolveBaseDuration(appointment);
        int target = base + BUFFER_AFTER_MINUTES;
        Integer current = appointment.getEstimatedDurationMinutes();

        // Evita somar o buffer mais de uma vez.
        if (current == null || current < target) {
            appointment.setEstimatedDurationMinutes(target);
        }
    }

    @Override
    protected void generateComplementaryRecord(Appointment appointment) {
        // Estética não exige registro extra.
    }

    /**
     * Retorna a duração base do agendamento em minutos.
     *
     * <p>Prioridade: duração do serviço oferecido → duração estimada do
     * agendamento → {@code 0} como fallback seguro.</p>
     */
    private int resolveBaseDuration(Appointment appointment) {
        OfferedService service = appointment.getOfferedService();
        if (service != null && service.getDurationMinutes() != null) {
            return service.getDurationMinutes();
        }
        Integer estimated = appointment.getEstimatedDurationMinutes();
        return estimated != null ? estimated : 0;
    }
}
