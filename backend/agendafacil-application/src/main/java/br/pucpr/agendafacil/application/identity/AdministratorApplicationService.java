package br.pucpr.agendafacil.application.identity;

import br.pucpr.agendafacil.application.dto.AdministratorResponse;
import br.pucpr.agendafacil.application.dto.CreateAdministratorRequest;
import br.pucpr.agendafacil.application.dto.UpdateAdministratorRequest;
import br.pucpr.agendafacil.application.mapper.AdministratorMapper;
import br.pucpr.agendafacil.domain.identity.AccessLevel;
import br.pucpr.agendafacil.domain.identity.Administrator;
import br.pucpr.agendafacil.domain.identity.port.AdministratorRepository;
import br.pucpr.agendafacil.domain.identity.port.PasswordHashingService;
import br.pucpr.agendafacil.domain.identity.port.UserRepository;
import br.pucpr.agendafacil.shared.exception.ConflictException;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Serviço de aplicação responsável pela gestão administrativa de administradores.
 *
 * <p>Centraliza os casos de uso de listagem, consulta, criação, atualização,
 * desativação e reativação de administradores.</p>
 *
 * <p>Também protege regras sensíveis da plataforma, como impedir que um
 * administrador desative a própria conta ou remova o último
 * {@link AccessLevel#SUPER_ADMIN} ativo.</p>
 */
@ApplicationScoped
public class AdministratorApplicationService {

    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final PasswordHashingService passwordHasher;
    private final AdministratorMapper administratorMapper;

    @Inject
    public AdministratorApplicationService(AdministratorRepository administratorRepository,
                                           UserRepository userRepository,
                                           PasswordHashingService passwordHasher,
                                           AdministratorMapper administratorMapper) {
        this.administratorRepository = administratorRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.administratorMapper = administratorMapper;
    }

    /**
     * Lista os administradores cadastrados.
     *
     * @param activeOnly indica se a listagem deve retornar apenas administradores ativos
     * @return lista de administradores encontrados
     */
    public List<AdministratorResponse> list(boolean activeOnly) {
        return administratorRepository.listAll(activeOnly).stream()
                .map(administratorMapper::toResponse)
                .toList();
    }

    /**
     * Busca um administrador pelo id.
     *
     * @param id identificador do administrador
     * @return dados do administrador encontrado
     * @throws NotFoundException quando o administrador não existir
     */
    public AdministratorResponse getById(Long id) {
        return administratorMapper.toResponse(loadOrThrow(id));
    }

    /**
     * Cria um novo administrador.
     *
     * <p>Antes do cadastro, o método verifica se o e-mail informado já está em
     * uso. A senha é armazenada usando o serviço de hash configurado na
     * aplicação.</p>
     *
     * @param req dados do administrador que será criado
     * @return administrador criado
     * @throws ConflictException quando já existir usuário com o e-mail informado
     */
    @Transactional
    public AdministratorResponse create(CreateAdministratorRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("E-mail já cadastrado no sistema.");
        }
        Administrator administrator = new Administrator(
                req.name(), req.email(), passwordHasher.hash(req.password()), req.accessLevel());
        administrator.setPhone(req.phone());
        administratorRepository.persist(administrator);
        return administratorMapper.toResponse(administrator);
    }

    /**
     * Atualiza os dados de um administrador existente.
     *
     * <p>A senha só é alterada quando uma nova senha é informada na requisição.
     * Caso contrário, a senha atual é mantida.</p>
     *
     * @param id identificador do administrador
     * @param req novos dados do administrador
     * @return administrador atualizado
     * @throws NotFoundException quando o administrador não existir
     */
    @Transactional
    public AdministratorResponse update(Long id, UpdateAdministratorRequest req) {
        Administrator administrator = loadOrThrow(id);
        administrator.setName(req.name());
        administrator.setPhone(req.phone());
        administrator.setAccessLevel(req.accessLevel());
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            administrator.setPassword(passwordHasher.hash(req.newPassword()));
        }
        Administrator saved = administratorRepository.update(administrator);
        return administratorMapper.toResponse(saved);
    }

    /**
     * Desativa o cadastro de um administrador.
     *
     * <p>O método impede que o administrador autenticado desative a própria
     * conta. Também impede a desativação do último
     * {@link AccessLevel#SUPER_ADMIN} ativo, para evitar que a plataforma fique
     * sem acesso administrativo principal.</p>
     *
     * @param id identificador do administrador que será desativado
     * @param actingAdminId identificador do administrador autenticado
     * @return administrador desativado
     * @throws ForbiddenException quando a operação puder causar bloqueio administrativo
     * @throws NotFoundException quando o administrador não existir
     */
    @Transactional
    public AdministratorResponse deactivate(Long id, Long actingAdminId) {
        if (id.equals(actingAdminId)) {
            throw new ForbiddenException("Não é possível desativar a si mesmo.");
        }
        Administrator administrator = loadOrThrow(id);
        if (administrator.getAccessLevel() == AccessLevel.SUPER_ADMIN
                && countActiveSuperAdmins() <= 1) {
            throw new ForbiddenException(
                    "Não é possível desativar o último SUPER_ADMIN ativo.");
        }
        administrator.deactivate();
        Administrator saved = administratorRepository.update(administrator);
        return administratorMapper.toResponse(saved);
    }

    /**
     * Reativa o cadastro de um administrador.
     *
     * @param id identificador do administrador
     * @return administrador reativado
     * @throws NotFoundException quando o administrador não existir
     */
    @Transactional
    public AdministratorResponse reactivate(Long id) {
        Administrator administrator = loadOrThrow(id);
        administrator.activate();
        Administrator saved = administratorRepository.update(administrator);
        return administratorMapper.toResponse(saved);
    }

    /**
     * Carrega um administrador pelo id ou lança exceção quando não encontrado.
     *
     * @param id identificador do administrador
     * @return administrador encontrado
     * @throws NotFoundException quando o administrador não existir
     */
    private Administrator loadOrThrow(Long id) {
        Administrator administrator = administratorRepository.getById(id);
        if (administrator == null) {
            throw new NotFoundException("Administrador não encontrado.");
        }
        return administrator;
    }

    /**
     * Conta quantos administradores {@link AccessLevel#SUPER_ADMIN} estão ativos.
     *
     * @return quantidade de super administradores ativos
     */
    private long countActiveSuperAdmins() {
        return administratorRepository.listAll(true).stream()
                .filter(a -> a.getAccessLevel() == AccessLevel.SUPER_ADMIN)
                .count();
    }
}