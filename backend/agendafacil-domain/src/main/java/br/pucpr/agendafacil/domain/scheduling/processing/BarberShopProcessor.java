package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

/**
 * Processador de agendamentos para barbearias e salões.
 *
 * <p>Representa o fluxo mais simples de processamento: o agendamento passa
 * pelas validações comuns, mas não exige regras extras, bloqueio de horários
 * adicionais ou registros complementares.</p>
 */
public class BarberShopProcessor extends AppointmentProcessor {

    @Override
    protected void applySpecificRules(Appointment appointment) {
        // Sem regras adicionais para este tipo de categoria.
    }

    @Override
    protected boolean shouldBlockAdditionalSlots() {
        return false;
    }

    @Override
    protected void blockAdditionalSlots(Appointment appointment) {
        // Não há bloqueio extra de agenda.
    }

    @Override
    protected void generateComplementaryRecord(Appointment appointment) {
        // Não há registro complementar para gerar.
    }
}