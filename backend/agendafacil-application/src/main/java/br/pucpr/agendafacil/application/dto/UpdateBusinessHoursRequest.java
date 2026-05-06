package br.pucpr.agendafacil.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados para atualização dos horários de funcionamento")
public record UpdateBusinessHoursRequest(
        @Schema(description = "Janelas de funcionamento que serão aplicadas ao estabelecimento",
                minItems = 1,
                examples = "[{\"dayOfWeek\":\"MONDAY\",\"startTime\":\"09:00\",\"endTime\":\"18:00\",\"active\":true}]",
                required = true)
        @NotEmpty(message = "Informe ao menos uma janela de funcionamento")
        @Valid
        List<BusinessHoursDTO> hours
) {}