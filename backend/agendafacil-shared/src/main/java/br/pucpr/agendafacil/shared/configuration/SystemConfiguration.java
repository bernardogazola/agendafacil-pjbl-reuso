package br.pucpr.agendafacil.shared.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração global do sistema.
 *
 * <p>Esta classe aplica o padrão Singleton: existe apenas uma instância de
 * configuração durante a execução da aplicação, acessada por meio de
 * {@link #getInstance()}.</p>
 *
 * <p>Ela concentra propriedades simples usadas pelo sistema, como timezone,
 * formato de horário e flags de módulos opcionais. Assim, essas configurações
 * ficam em um único ponto de acesso, evitando que diferentes partes da aplicação
 * mantenham valores separados para a mesma informação.</p>
 *
 * <p>Neste projeto, a implementação clássica do Singleton foi mantida para
 * demonstrar o padrão de projeto de forma explícita. Em uma aplicação Quarkus
 * real, essa responsabilidade também poderia ser controlada pelo CDI com
 * {@code @ApplicationScoped}.</p>
 */
public class SystemConfiguration {

    private static SystemConfiguration instance;

    private final Map<String, Object> properties = new HashMap<>();

    private SystemConfiguration() {
        properties.put("timezone", "America/Sao_Paulo");
        properties.put("timeFormat", "HH:mm");
        properties.put("module.reviews.enabled", false);
        properties.put("module.promotions.enabled", false);
        properties.put("module.notifications.enabled", true);
    }

    /**
     * Retorna a instância única da configuração do sistema.
     *
     * <p>A instância é criada apenas na primeira chamada do método.</p>
     *
     * @return instância única de {@link SystemConfiguration}
     */
    public static synchronized SystemConfiguration getInstance() {
        if (instance == null) {
            instance = new SystemConfiguration();
        }
        return instance;
    }

    /**
     * Busca o valor de uma configuração pela chave.
     *
     * @param key chave da configuração
     * @return valor associado à chave ou {@code null} quando não existir
     */
    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * Verifica se uma configuração booleana está habilitada.
     *
     * @param module chave da configuração do módulo
     * @return {@code true} quando o valor da chave for {@code Boolean.TRUE}
     */
    public boolean isEnabled(String module) {
        return Boolean.TRUE.equals(properties.get(module));
    }

    /**
     * Define ou atualiza o valor de uma configuração.
     *
     * @param key chave da configuração
     * @param value novo valor da configuração
     */
    public void set(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Reinicia a instância única da configuração.
     *
     * <p>Este método existe apenas para facilitar testes automatizados.</p>
     */
    static synchronized void reset() {
        instance = null;
    }
}