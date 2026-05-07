package br.pucpr.agendafacil.app.bootstrap;

import br.pucpr.agendafacil.shared.configuration.SystemConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Testa se o bootstrap carrega as configurações globais no Singleton durante a
 * inicialização da aplicação.
 */
@QuarkusTest
class SystemConfigurationBootstrapTest {

    @Test
    void singleton_isPopulatedFromApplicationProperties() {
        SystemConfiguration config = SystemConfiguration.getInstance();

        assertEquals("America/Sao_Paulo", config.get("timezone"));
        assertEquals("HH:mm", config.get("timeFormat"));
        assertTrue(config.isEnabled("module.notifications.enabled"));
        assertFalse(config.isEnabled("module.reviews.enabled"));
        assertFalse(config.isEnabled("module.promotions.enabled"));
    }
}