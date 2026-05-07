package br.pucpr.agendafacil.app.bootstrap;

import br.pucpr.agendafacil.adapter.out.persistence.business.BusinessHoursPanacheRepository;
import br.pucpr.agendafacil.adapter.out.persistence.business.BusinessPanacheRepository;
import br.pucpr.agendafacil.adapter.out.persistence.business.OfferedServicePanacheRepository;
import br.pucpr.agendafacil.adapter.out.persistence.identity.AdministratorPanacheRepository;
import br.pucpr.agendafacil.adapter.out.persistence.identity.CustomerPanacheRepository;
import br.pucpr.agendafacil.adapter.out.persistence.identity.UserPanacheRepository;
import br.pucpr.agendafacil.app.security.PasswordHasher;
import br.pucpr.agendafacil.application.business.BusinessApplicationService;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.identity.AccessLevel;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.domain.scheduling.cancellation.CancellationPolicyType;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;

/**
 * Inicializa dados de demonstração no banco da aplicação.
 *
 * <p>O seeder é executado na inicialização do Quarkus e só popula o banco
 * quando a carga de demonstração está habilitada e ainda não existem usuários
 * cadastrados.</p>
 *
 * <p>Os dados criados incluem donos de estabelecimento, estabelecimentos,
 * horários de funcionamento, serviços oferecidos e clientes de exemplo. Essa
 * carga facilita testes manuais e apresentação da aplicação sem exigir cadastro
 * inicial pelo usuário.</p>
 */
@ApplicationScoped
public class DemoDataSeeder {

    private static final String DEMO_PASSWORD = "Demo@2026";

    private final UserPanacheRepository userRepository;
    private final AdministratorPanacheRepository administratorRepository;
    private final CustomerPanacheRepository customerRepository;
    private final BusinessPanacheRepository businessRepository;
    private final OfferedServicePanacheRepository offeredServiceRepository;
    private final BusinessHoursPanacheRepository businessHoursRepository;
    private final BusinessApplicationService businessService;
    private final PasswordHasher passwordHasher;

    @ConfigProperty(name = "agendafacil.demo.seed.enabled", defaultValue = "true")
    boolean enabled;

    @Inject
    public DemoDataSeeder(UserPanacheRepository userRepository,
                          AdministratorPanacheRepository administratorRepository,
                          CustomerPanacheRepository customerRepository,
                          BusinessPanacheRepository businessRepository,
                          OfferedServicePanacheRepository offeredServiceRepository,
                          BusinessHoursPanacheRepository businessHoursRepository,
                          BusinessApplicationService businessService,
                          PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.administratorRepository = administratorRepository;
        this.customerRepository = customerRepository;
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.businessService = businessService;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Executa a carga de demonstração durante a inicialização da aplicação.
     *
     * <p>A carga é ignorada quando estiver desabilitada por configuração ou
     * quando o banco já possuir usuários cadastrados.</p>
     *
     * @param ev evento de inicialização do Quarkus
     */
    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (!enabled) {
            Log.info("Seed demo: desabilitado por configuração, pulando.");
            return;
        }
        if (userRepository.count() > 0) {
            Log.info("Seed demo: banco já populado, pulando.");
            return;
        }

        String encoded = passwordHasher.hash(DEMO_PASSWORD);

        Administrator superAdmin = new Administrator(
                "Administrador da Plataforma", "admin@demo.com", encoded, AccessLevel.SUPER_ADMIN);
        administratorRepository.persist(superAdmin);

        Business barbearia = seedBusiness(
                "João Barbeiro", "owner1@demo.com", encoded,
                "Barbearia do João", "contato@barbearia-joao.com.br",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC,
                CancellationPolicyType.FREE);

        Business clinica = seedBusiness(
                "Dra. Carla Silva", "owner2@demo.com", encoded,
                "Clínica Bem-Estar", "contato@bem-estar.com.br",
                BusinessCategory.CLINIC, BusinessPlan.PROFESSIONAL,
                CancellationPolicyType.DEADLINE);

        Business personal = seedBusiness(
                "Ana Personal", "owner3@demo.com", encoded,
                "Personal Trainer Ana Fit", "contato@anafit.com.br",
                BusinessCategory.PERSONAL_TRAINER, BusinessPlan.PREMIUM,
                CancellationPolicyType.FEE_BASED);

        // A barbearia também atende no sábado de manhã.
        businessHoursRepository.findByBusinessAndDayOfWeek(barbearia.getId(), DayOfWeek.SATURDAY)
                .ifPresent(bh -> {
                    bh.setStartTime(LocalTime.of(9, 0));
                    bh.setEndTime(LocalTime.of(13, 0));
                    bh.activate();
                });

        seedService(barbearia, "Corte masculino",
                "Corte tradicional de cabelo, tesoura e máquina.",
                new BigDecimal("50.00"), 30, PricingPolicyType.FIXED);
        seedService(barbearia, "Barba completa",
                "Modelagem, aparamento e toalha quente.",
                new BigDecimal("40.00"), 30, PricingPolicyType.DURATION_BASED);

        seedService(clinica, "Consulta geral",
                "Consulta de rotina com clínico geral.",
                new BigDecimal("180.00"), 60, PricingPolicyType.FIXED);
        seedService(clinica, "Check-up preventivo",
                "Avaliação completa com desconto promocional.",
                new BigDecimal("350.00"), 90, PricingPolicyType.DISCOUNTED);

        seedService(personal, "Avaliação física",
                "Anamnese e medidas iniciais.",
                new BigDecimal("120.00"), 60, PricingPolicyType.FIXED);
        seedService(personal, "Sessão de treino",
                "Treino personalizado de 1h com acompanhamento.",
                new BigDecimal("100.00"), 60, PricingPolicyType.DURATION_BASED);

        seedCustomer("Maria Cliente", "cliente1@demo.com", encoded);
        seedCustomer("Pedro Cliente", "cliente2@demo.com", encoded);

        Log.infof("Seed demo: %d users, %d businesses, %d services inseridos.",
                userRepository.count(),
                businessRepository.count(),
                offeredServiceRepository.count());
    }

    /**
     * Cria um dono de estabelecimento, seu negócio e os horários padrão.
     *
     * @param ownerName nome do dono
     * @param ownerEmail e-mail de login do dono
     * @param encodedPassword senha já codificada
     * @param tradeName nome fantasia do estabelecimento
     * @param businessEmail e-mail de contato do estabelecimento
     * @param category categoria do estabelecimento
     * @param plan plano contratado
     * @param cancellationPolicyType política de cancelamento configurada
     * @return estabelecimento criado
     */
    private Business seedBusiness(String ownerName, String ownerEmail, String encodedPassword,
                                  String tradeName, String businessEmail,
                                  BusinessCategory category, BusinessPlan plan,
                                  CancellationPolicyType cancellationPolicyType) {
        Administrator owner = new Administrator(
                ownerName, ownerEmail, encodedPassword, AccessLevel.BUSINESS_ADMIN);
        administratorRepository.persist(owner);

        Business business = new Business(tradeName, businessEmail, category, plan, owner);
        business.setCancellationPolicyType(cancellationPolicyType);
        businessRepository.persist(business);
        businessService.initializeDefaultHours(business);
        return business;
    }

    /**
     * Cria um serviço de demonstração para um estabelecimento.
     *
     * @param business estabelecimento dono do serviço
     * @param name nome do serviço
     * @param description descrição do serviço
     * @param basePrice preço base
     * @param durationMinutes duração estimada em minutos
     * @param pricingPolicyType política de precificação configurada
     */
    private void seedService(Business business, String name, String description,
                             BigDecimal basePrice, int durationMinutes,
                             PricingPolicyType pricingPolicyType) {
        OfferedService service = new OfferedService(name, basePrice, durationMinutes, business);
        service.setDescription(description);
        service.setPricingPolicyType(pricingPolicyType);
        offeredServiceRepository.persist(service);
    }

    /**
     * Cria um cliente de demonstração.
     *
     * <p>O e-mail é habilitado como canal inicial de notificação para permitir
     * testar os fluxos de confirmação e cancelamento de agendamento.</p>
     *
     * @param name nome do cliente
     * @param email e-mail do cliente
     * @param encodedPassword senha já codificada
     */
    private void seedCustomer(String name, String email, String encodedPassword) {
        Customer customer = new Customer(name, email, encodedPassword);
        customer.setNotificationPreferences(EnumSet.of(NotificationChannel.EMAIL));
        customerRepository.persist(customer);
    }
}