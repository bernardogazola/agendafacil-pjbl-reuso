package br.pucpr.agendafacil.domain.scheduling.processing;

import br.pucpr.agendafacil.domain.business.Business;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Factory responsável por escolher o processador de agendamentos adequado para
 * cada tipo de estabelecimento.
 *
 * <p>Esta classe complementa o uso do Template Method: o fluxo geral está em
 * {@link AppointmentProcessor}, enquanto esta factory centraliza a escolha da
 * subclasse que executará os passos específicos.</p>
 *
 * <p>Com isso, o serviço que cria o agendamento não precisa conhecer as
 * diferenças internas entre barbearia, clínica e estética.</p>
 */
@ApplicationScoped
public class AppointmentProcessorFactory {

    /**
     * Retorna o processador correspondente à categoria do estabelecimento.
     */
    public AppointmentProcessor resolve(Business business) {
        return switch (business.getCategory()) {
            case BARBER_SHOP, SALON, PERSONAL_TRAINER, OTHER -> new BarberShopProcessor();
            case CLINIC, PSYCHOLOGIST -> new ClinicProcessor();
            case AESTHETICS -> new AestheticsProcessor();
        };
    }
}
