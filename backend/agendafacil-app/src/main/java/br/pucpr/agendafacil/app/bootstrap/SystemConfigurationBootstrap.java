package br.pucpr.agendafacil.app.bootstrap;

import br.pucpr.agendafacil.shared.configuration.SystemConfiguration;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrega as configurações globais da aplicação no {@link SystemConfiguration}.
 *
 * <p>Este bootstrap roda na inicialização do Quarkus, lê os valores resolvidos
 * pelo MicroProfile Config e popula o Singleton usado pelo restante do sistema.</p>
 *
 * <p>A classe fica no módulo executável porque depende de CDI, Quarkus e
 * MicroProfile Config. Com isso, {@code SystemConfiguration} continua livre de
 * dependências de framework.</p>
 */
@ApplicationScoped
@Startup
public class SystemConfigurationBootstrap {

    private static final Logger log = Logger.getLogger(SystemConfigurationBootstrap.class);

    @ConfigProperty(name = "agendafacil.timezone", defaultValue = "America/Sao_Paulo")
    String timezone;

    @ConfigProperty(name = "agendafacil.time-format", defaultValue = "HH:mm")
    String timeFormat;

    @ConfigProperty(name = "agendafacil.module.reviews.enabled", defaultValue = "false")
    boolean reviewsEnabled;

    @ConfigProperty(name = "agendafacil.module.promotions.enabled", defaultValue = "false")
    boolean promotionsEnabled;

    @ConfigProperty(name = "agendafacil.module.notifications.enabled", defaultValue = "true")
    boolean notificationsEnabled;

    /**
     * Monta o conjunto de configurações globais e carrega no Singleton.
     */
    public void init() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("timezone", timezone);
        values.put("timeFormat", timeFormat);
        values.put("module.reviews.enabled", reviewsEnabled);
        values.put("module.promotions.enabled", promotionsEnabled);
        values.put("module.notifications.enabled", notificationsEnabled);
        SystemConfiguration.getInstance().loadAll(values);
        log.infof("SystemConfiguration carregado de application.properties: %d chave(s).",
                values.size());
    }

    /**
     * Executa o carregamento das configurações quando a aplicação inicia.
     *
     * @param ev evento de inicialização do Quarkus
     */
    void onStartup(@Observes StartupEvent ev) {
        init();
    }
}