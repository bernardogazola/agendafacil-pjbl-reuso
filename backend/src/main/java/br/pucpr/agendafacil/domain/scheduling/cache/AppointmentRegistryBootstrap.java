package br.pucpr.agendafacil.domain.scheduling.cache;

import br.pucpr.agendafacil.domain.scheduling.Appointment;
import br.pucpr.agendafacil.domain.scheduling.port.AppointmentRepository;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inicializa o cache de agendamentos quando a aplicação é iniciada.
 *
 * <p>Como o cache fica em memória, ele precisa ser preenchido novamente sempre
 * que o processo da aplicação sobe. Esta classe busca no banco os agendamentos
 * ativos e carrega esses dados no {@link AppointmentRegistry} antes do uso
 * normal do sistema.</p>
 *
 * <p>O {@link AppointmentRegistry} continua sendo acessado por
 * {@code getInstance()}. Esta classe apenas prepara o cache inicial para que
 * ele comece consistente com os dados persistidos.</p>
 */
@ApplicationScoped
@Startup
public class AppointmentRegistryBootstrap {

    private static final Logger log = Logger.getLogger(AppointmentRegistryBootstrap.class);

    private final AppointmentRepository repository;

    @Inject
    public AppointmentRegistryBootstrap(AppointmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void init() {
        AppointmentRegistry registry = AppointmentRegistry.getInstance();
        registry.clear();
        List<Appointment> active = repository.findActiveAfter(LocalDateTime.now());
        for (Appointment a : active) {
            registry.register(a);
        }
        log.infof("AppointmentRegistry populado com %d agendamento(s) ativo(s).", active.size());
    }

    void onStartup(@Observes StartupEvent ev) {
        init();
    }
}