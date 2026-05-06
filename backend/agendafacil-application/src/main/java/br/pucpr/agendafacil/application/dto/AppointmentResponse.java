package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.scheduling.AppointmentStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados de um agendamento do cliente em um estabelecimento")
public record AppointmentResponse(
        @Schema(description = "Identificador do agendamento",examples = "42", readOnly = true)
        Long id,

        @Schema(description = "Identificador do estabelecimento", examples = "10")
        Long businessId,

        @Schema(description = "Nome fantasia do estabelecimento", examples = "Barbearia Central")
        String businessName,

        @Schema(description = "Identificador do serviço agendado", examples = "5")
        Long serviceId,

        @Schema(description = "Nome do serviço agendado", examples = "Corte masculino")
        String serviceName,

        @Schema(description = "Identificador do cliente", examples = "7")
        Long customerId,

        @Schema(description = "Nome do cliente", examples = "João Silva")
        String customerName,

        @Schema(description = "Data e hora do agendamento",
                format = "date-time",
                examples = "2026-05-20T14:30:00")
        LocalDateTime scheduledAt,

        @Schema(description = "Duração efetiva reservada na agenda, em minutos", examples = "45")
        Integer estimatedDurationMinutes,

        @Schema(description = "Preço final pago pelo agendamento", examples = "89.90")
        BigDecimal pricePaid,

        @Schema(description = "Status atual do agendamento", examples = "SCHEDULED")
        AppointmentStatus status,

        @Schema(description = "Observações informadas pelo cliente",
                nullable = true,
                examples = "Preferência por atendimento sem máquina")
        String notes,

        @Schema(description = "Referência da ficha clínica, quando gerada para clínicas e psicólogos",
                nullable = true,
                examples = "FICHA-20260520-7")
        String clinicalRecordRef
) {}