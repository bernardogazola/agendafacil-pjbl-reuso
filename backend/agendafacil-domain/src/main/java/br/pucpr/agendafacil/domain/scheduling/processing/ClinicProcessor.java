package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.format.DateTimeFormatter;

/**
 * Processador de agendamentos para clínicas.
 *
 * <p>Esta classe especializa o Template Method de {@link AppointmentProcessor}
 * com regras próprias de atendimentos clínicos. Além das validações comuns,
 * exige antecedência mínima, obriga o preenchimento do motivo da consulta e
 * gera uma referência de ficha clínica para o agendamento.</p>
 *
 * <p>A referência gerada não representa ainda um prontuário completo. Ela serve
 * como identificação inicial para uma futura integração com o controle de
 * fichas ou prontuários.</p>
 */
public class ClinicProcessor extends AppointmentProcessor {

    private static final int CLINIC_MIN_LEAD_HOURS = 2;
    private static final DateTimeFormatter REF_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String REF_PREFIX = "FICHA-";

    @Override
    protected int minimumLeadHours() {
        return CLINIC_MIN_LEAD_HOURS;
    }

    @Override
    protected void applySpecificRules(Appointment appointment) {
        String notes = appointment.getNotes();
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException(
                    "Para clínica é obrigatório informar o motivo da consulta no campo de observações");
        }
    }

    @Override
    protected boolean shouldBlockAdditionalSlots() {
        return false;
    }

    @Override
    protected void blockAdditionalSlots(Appointment appointment) {
        // Clínica utiliza apenas o slot do procedimento.
    }

    @Override
    protected void generateComplementaryRecord(Appointment appointment) {
        // Mantém a referência existente caso o processamento seja chamado novamente.
        if (appointment.getClinicalRecordRef() != null) return;

        String day = appointment.getScheduledAt().toLocalDate().format(REF_DATE);
        String customerKey = appointment.getCustomer() == null
                || appointment.getCustomer().getId() == null
                ? "NEW"
                : appointment.getCustomer().getId().toString();
        appointment.setClinicalRecordRef(REF_PREFIX + day + "-" + customerKey);
    }
}