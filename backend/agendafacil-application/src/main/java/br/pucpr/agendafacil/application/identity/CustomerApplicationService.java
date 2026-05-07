package br.pucpr.agendafacil.application.identity;

import br.pucpr.agendafacil.application.dto.CustomerResponse;
import br.pucpr.agendafacil.application.dto.UpdateCustomerRequest;
import br.pucpr.agendafacil.application.mapper.CustomerMapper;
import br.pucpr.agendafacil.domain.identity.Customer;
import br.pucpr.agendafacil.domain.identity.port.CustomerRepository;
import br.pucpr.agendafacil.domain.notification.NotificationChannel;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.EnumSet;
import java.util.List;

/**
 * Serviço de aplicação responsável pela gestão administrativa de clientes.
 *
 * <p>Centraliza os casos de uso de listagem, consulta, atualização,
 * desativação e reativação de clientes.</p>
 */
@ApplicationScoped
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Inject
    public CustomerApplicationService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    /**
     * Lista os clientes cadastrados.
     *
     * @param activeOnly indica se a listagem deve retornar apenas clientes ativos
     * @return lista de clientes encontrados
     */
    public List<CustomerResponse> list(boolean activeOnly) {
        return customerRepository.listAll(activeOnly).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador do cliente
     * @return dados do cliente encontrado
     * @throws NotFoundException quando o cliente não existir
     */
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(loadOrThrow(id));
    }

    /**
     * Atualiza os dados cadastrais de um cliente.
     *
     * <p>As preferências de notificação enviadas substituem o conjunto anterior.
     * Quando não forem informadas, o cliente fica sem canais habilitados.</p>
     *
     * @param id identificador do cliente
     * @param req novos dados do cliente
     * @return cliente atualizado
     * @throws NotFoundException quando o cliente não existir
     */
    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest req) {
        Customer customer = loadOrThrow(id);
        customer.setName(req.name());
        customer.setPhone(req.phone());
        customer.setBirthDate(req.birthDate());
        customer.setNotificationPreferences(
                req.notificationPreferences() == null
                        ? EnumSet.noneOf(NotificationChannel.class)
                        : EnumSet.copyOf(req.notificationPreferences())
        );
        Customer saved = customerRepository.update(customer);
        return customerMapper.toResponse(saved);
    }

    /**
     * Desativa o cadastro de um cliente.
     *
     * @param id identificador do cliente
     * @return cliente desativado
     * @throws NotFoundException quando o cliente não existir
     */
    @Transactional
    public CustomerResponse deactivate(Long id) {
        Customer customer = loadOrThrow(id);
        customer.deactivate();
        Customer saved = customerRepository.update(customer);
        return customerMapper.toResponse(saved);
    }

    /**
     * Reativa o cadastro de um cliente.
     *
     * @param id identificador do cliente
     * @return cliente reativado
     * @throws NotFoundException quando o cliente não existir
     */
    @Transactional
    public CustomerResponse reactivate(Long id) {
        Customer customer = loadOrThrow(id);
        customer.activate();
        Customer saved = customerRepository.update(customer);
        return customerMapper.toResponse(saved);
    }

    /**
     * Carrega um cliente pelo id ou lança exceção quando não encontrado.
     *
     * @param id identificador do cliente
     * @return cliente encontrado
     * @throws NotFoundException quando o cliente não existir
     */
    private Customer loadOrThrow(Long id) {
        Customer customer = customerRepository.getById(id);
        if (customer == null) {
            throw new NotFoundException("Cliente não encontrado.");
        }
        return customer;
    }
}