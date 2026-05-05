package br.pucpr.agendafacil.shared.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemConfigurationTest {

    @BeforeEach
    void resetSingleton() {
        SystemConfiguration.reset();
    }

    @Test
    void getInstance_returnsSameInstance() {
        SystemConfiguration a = SystemConfiguration.getInstance();
        SystemConfiguration b = SystemConfiguration.getInstance();
        assertSame(a, b, "Singleton deve retornar sempre a mesma instância");
    }

    @Test
    void defaults_areLoaded() {
        SystemConfiguration config = SystemConfiguration.getInstance();
        assertEquals("America/Sao_Paulo", config.get("timezone"));
        assertEquals("HH:mm", config.get("timeFormat"));
        assertTrue(config.isEnabled("module.notifications.enabled"));
        assertFalse(config.isEnabled("module.reviews.enabled"));
        assertFalse(config.isEnabled("module.promotions.enabled"));
    }

    @Test
    void set_persistsAcrossGetInstance() {
        SystemConfiguration.getInstance().set("module.reviews.enabled", true);
        assertTrue(SystemConfiguration.getInstance().isEnabled("module.reviews.enabled"));
    }

    @Test
    void isEnabled_returnsFalseForUnknownKey() {
        assertFalse(SystemConfiguration.getInstance().isEnabled("module.does.not.exist"));
    }
}