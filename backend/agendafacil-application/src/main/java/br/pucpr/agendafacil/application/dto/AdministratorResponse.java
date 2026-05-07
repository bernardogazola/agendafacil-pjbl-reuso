package br.pucpr.agendafacil.application.dto;

import br.pucpr.agendafacil.domain.identity.AccessLevel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Resposta com os dados de um administrador cadastrado.
 */
@Schema(description = "Dados de um administrador cadastrado na plataforma")
public record AdministratorResponse(
        @Schema(description = "Identificador do administrador", examples = "3", readOnly = true)
        Long id,

        @Schema(description = "Nome completo do administrador", examples = "Carlos Admin")
        String name,

        @Schema(description = "E-mail cadastrado do administrador", format = "email",
                examples = "admin@email.com")
        String email,

        @Schema(description = "Telefone do administrador", nullable = true,
                examples = "+5541999999999")
        String phone,

        @Schema(description = "Nível de acesso do administrador", examples = "BUSINESS_ADMIN")
        AccessLevel accessLevel,

        @Schema(description = "Indica se o cadastro do administrador está ativo",
                examples = "true")
        boolean active
) {}