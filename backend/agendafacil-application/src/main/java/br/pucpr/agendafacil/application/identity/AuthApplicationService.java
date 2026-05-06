package br.pucpr.agendafacil.application.identity;

import br.pucpr.agendafacil.application.business.BusinessApplicationService;
import br.pucpr.agendafacil.application.dto.LoginRequest;
import br.pucpr.agendafacil.application.dto.LoginResponse;
import br.pucpr.agendafacil.application.dto.SignupCustomerRequest;
import br.pucpr.agendafacil.application.dto.SignupOwnerRequest;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.identity.AccessLevel;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.identity.port.*;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.shared.exception.ConflictException;
import br.pucpr.agendafacil.shared.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.EnumSet;

/**
 * Serviço de aplicação responsável pelos fluxos de autenticação.
 *
 * <p>Centraliza o cadastro de clientes, o cadastro de donos com estabelecimento
 * e o login de usuários já cadastrados.</p>
 *
 * <p>Após autenticar o usuário, o serviço identifica se ele é cliente ou dono
 * de estabelecimento para emitir o token com o papel correto.</p>
 */
@ApplicationScoped
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AdministratorRepository administratorRepository;
    private final BusinessRepository businessRepository;
    private final BusinessApplicationService businessService;
    private final PasswordHashingService passwordHasher;
    private final TokenIssuer jwtService;

    @Inject
    public AuthApplicationService(UserRepository userRepository,
                                  CustomerRepository customerRepository,
                                  AdministratorRepository administratorRepository,
                                  BusinessRepository businessRepository,
                                  BusinessApplicationService businessService,
                                  PasswordHashingService passwordHasher,
                                  TokenIssuer jwtService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.administratorRepository = administratorRepository;
        this.businessRepository = businessRepository;
        this.businessService = businessService;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
    }

    /**
     * Cadastra um novo cliente na plataforma.
     *
     * <p>O método valida se o e-mail ainda não está cadastrado, cria o cliente com
     * senha criptografada, define o e-mail como canal padrão de notificação e
     * retorna o token de autenticação.</p>
     *
     * @param req dados de cadastro do cliente
     * @return resposta de login com token e dados básicos do cliente criado
     */
    @Transactional
    public LoginResponse signupCustomer(SignupCustomerRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("E-mail já cadastrado no sistema.");
        }
        Customer customer = new Customer(req.name(), req.email(), passwordHasher.hash(req.password()));
        customer.setBirthDate(req.birthDate());
        customer.setPhone(req.phone());
        customer.setNotificationPreferences(EnumSet.of(NotificationChannel.EMAIL));
        customerRepository.persist(customer);
        String token = jwtService.issue(customer, "customer");
        return LoginResponse.customer(token, customer.getId(), customer.getEmail(), customer.getName());
    }

    /**
     * Cadastra um dono de estabelecimento e seu primeiro estabelecimento.
     *
     * <p>O cadastro é feito em uma única transação: primeiro o administrador é
     * criado, depois o estabelecimento é vinculado a ele e, por fim, os horários
     * padrão de funcionamento são inicializados.</p>
     *
     * @param req dados do administrador e do estabelecimento
     * @return resposta de login com token, dados do administrador e id do estabelecimento
     */
    @Transactional
    public LoginResponse signupOwnerWithBusiness(SignupOwnerRequest req) {
        if (userRepository.existsByEmail(req.ownerEmail())) {
            throw new ConflictException("E-mail do administrador já cadastrado no sistema.");
        }
        Administrator admin = new Administrator(
                req.ownerName(), req.ownerEmail(),
                passwordHasher.hash(req.ownerPassword()),
                AccessLevel.BUSINESS_ADMIN);
        administratorRepository.persist(admin);

        Business business = new Business(
                req.businessTradeName(), req.businessEmail(),
                req.category(), req.plan(), admin);
        businessRepository.persist(business);
        businessService.initializeDefaultHours(business);

        String token = jwtService.issue(admin, "owner");
        return LoginResponse.owner(token, admin.getId(), admin.getEmail(), admin.getName(), business.getId());
    }

    /**
     * Autentica um usuário já cadastrado.
     *
     * <p>O método busca o usuário pelo e-mail, valida a senha informada e emite um
     * token com o papel correspondente ao tipo de usuário autenticado.</p>
     *
     * @param req dados de login
     * @return resposta de login com token e dados básicos do usuário
     */
    @Transactional
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));
        if (!passwordHasher.verify(req.password(), user.getPassword())) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }
        String role = detectRole(user);
        String token = jwtService.issue(user, role);
        if ("owner".equals(role)) {
            Long businessId = businessRepository.findByOwnerId(user.getId()).stream()
                    .findFirst().map(Business::getId).orElse(null);
            return LoginResponse.owner(token, user.getId(), user.getEmail(), user.getName(), businessId);
        }
        return LoginResponse.customer(token, user.getId(), user.getEmail(), user.getName());
    }

    /**
     * Resolve o papel usado no token a partir do tipo concreto do usuário.
     *
     * @param user usuário autenticado
     * @return papel usado na autorização
     */
    private String detectRole(User user) {
        return switch (user) {
            case Customer ignored -> "customer";
            case Administrator ignored -> "owner";
            default -> throw new UnauthorizedException(
                    "Tipo de usuário não suportado para autenticação.");
        };
    }
}