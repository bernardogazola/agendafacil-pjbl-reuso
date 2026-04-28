package br.pucpr.agendafacil.domain.scheduling.cache;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cache em memória dos agendamentos ativos da aplicação.
 *
 * <p>Esta classe usa o padrão Singleton para manter uma única instância do
 * cache durante a execução do sistema. Isso evita que partes diferentes da
 * aplicação consultem estados diferentes e acabem tomando decisões
 * inconsistentes sobre conflitos de horário.</p>
 *
 * <p>O cache serve como apoio para consultas rápidas, principalmente na
 * verificação de horários já ocupados. O banco de dados continua sendo a fonte
 * principal dos dados.</p>
 *
 * <p>A lista interna usa {@link CopyOnWriteArrayList} para permitir leituras
 * seguras mesmo quando houver alterações no cache. Como as consultas tendem a
 * acontecer com mais frequência do que os registros e remoções, esse custo é
 * aceitável neste caso.</p>
 */
public class AppointmentRegistry {

    private static AppointmentRegistry instance;

    private final List<Appointment> cache = new CopyOnWriteArrayList<>();

    private AppointmentRegistry() {
    }

    public static synchronized AppointmentRegistry getInstance() {
        if (instance == null) {
            instance = new AppointmentRegistry();
        }
        return instance;
    }

    public void register(Appointment appointment) {
        cache.add(appointment);
    }

    public void unregister(Appointment appointment) {
        cache.remove(appointment);
    }

    public List<Appointment> forDay(LocalDate day, Long businessId) {
        return cache.stream()
                .filter(a -> a.getScheduledAt().toLocalDate().equals(day))
                .filter(a -> a.getBusiness() != null
                        && a.getBusiness().getId() != null
                        && a.getBusiness().getId().equals(businessId))
                .toList();
    }

    public void clear() {
        cache.clear();
    }

    /**
     * Limpa a instância do Singleton entre execuções de teste.
     */
    static synchronized void reset() {
        if (instance != null) {
            instance.cache.clear();
        }
        instance = null;
    }
}
