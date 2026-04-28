package br.pucpr.agendafacil.domain.scheduling.port;

import br.pucpr.agendafacil.domain.scheduling.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelo acesso aos dados de {@link Appointment}.
 *
 * <p>Esta interface funciona como uma porta de saída do domínio: as regras de
 * negócio dependem dela para consultar e persistir agendamentos, sem conhecer
 * os detalhes da tecnologia usada para armazenar os dados.</p>
 */
public interface AppointmentRepository {

    /**
     * Busca um agendamento pelo id.
     *
     * @param id identificador do agendamento
     * @return o agendamento encontrado ou {@code null} quando não existir
     */
    Appointment findById(Long id);

    /**
     * Busca os agendamentos de um estabelecimento em uma data específica.
     *
     * @param businessId identificador do estabelecimento
     * @param day data da consulta
     * @return lista de agendamentos encontrados para o dia informado
     */
    List<Appointment> findByBusinessAndDate(Long businessId, LocalDate day);

    /**
     * Busca os agendamentos de um estabelecimento dentro de um intervalo de datas.
     *
     * @param businessId identificador do estabelecimento
     * @param from data inicial do intervalo
     * @param to data final do intervalo
     * @return lista de agendamentos encontrados no período informado
     */
    List<Appointment> findByBusinessAndDateRange(Long businessId, LocalDate from, LocalDate to);

    /**
     * Busca todos os agendamentos de um cliente, ordenados do mais recente para
     * o mais antigo.
     *
     * @param customerId identificador do cliente
     * @return lista de agendamentos do cliente
     */
    List<Appointment> findByCustomerId(Long customerId);

    /**
     * Busca os agendamentos ativos de um cliente.
     *
     * <p>São considerados ativos os agendamentos com status
     * {@code SCHEDULED} ou {@code CONFIRMED}.</p>
     *
     * @param customerId identificador do cliente
     * @return lista de agendamentos ativos do cliente
     */
    List<Appointment> findActiveByCustomerId(Long customerId);

    /**
     * Busca um agendamento pelo id, garantindo que ele pertence ao
     * cliente informado.
     *
     * @param appointmentId identificador do agendamento
     * @param customerId identificador do cliente
     * @return o agendamento encontrado, quando existir e pertencer ao cliente
     */
    Optional<Appointment> findByIdAndCustomerId(Long appointmentId, Long customerId);

    /**
     * Busca agendamentos ativos com início após o instante informado.
     *
     * <p>São considerados ativos os agendamentos com status {@code SCHEDULED}
     * ou {@code CONFIRMED}.</p>
     *
     * @param cutoff instante usado como limite inferior da consulta
     * @return lista de agendamentos ativos encontrados
     */
    List<Appointment> findActiveAfter(LocalDateTime cutoff);

    /**
     * Persiste um novo agendamento ou atualiza um agendamento existente.
     *
     * @param appointment agendamento a ser salvo
     */
    void persist(Appointment appointment);
}
