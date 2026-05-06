package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Horários disponíveis para agendamento em uma data")
public record AvailableSlotsResponse(
        @Schema(description = "Identificador do estabelecimento", examples = "10")
        Long businessId,

        @Schema(description = "Identificador do serviço", examples = "5")
        Long serviceId,

        @Schema(description = "Data consultada", format = "date", examples = "2026-05-20")
        LocalDate date,

        @Schema(description = "Horários disponíveis para agendamento", examples = "[\"09:00\", \"09:30\", \"10:00\"]")
        List<LocalTime> slots
) {}