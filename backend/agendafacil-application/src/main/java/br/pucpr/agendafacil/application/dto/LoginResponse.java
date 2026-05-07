package br.pucpr.agendafacil.application.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com token e dados básicos do usuário")
public record LoginResponse(
        @Schema(description = "Token de autenticação emitido para o usuário",
                readOnly = true,
                examples = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Papel do usuário autenticado", examples = {"customer", "owner", "admin"})
        String role,

        @Schema(description = "Identificador do usuário autenticado", examples = "7", readOnly = true)
        Long userId,

        @Schema(description = "E-mail do usuário autenticado",
                format = "email",
                examples = "cliente@email.com")
        String email,

        @Schema(description = "Nome do usuário autenticado", examples = "João Silva")
        String name,

        @Schema(description = "Identificador do estabelecimento, presente apenas para donos",
                nullable = true,
                examples = "10")
        Long businessId
) {
    public static LoginResponse customer(String token, Long userId, String email, String name) {
        return new LoginResponse(token, "customer", userId, email, name, null);
    }

    public static LoginResponse owner(String token, Long userId, String email, String name, Long businessId) {
        return new LoginResponse(token, "owner", userId, email, name, businessId);
    }

    public static LoginResponse admin(String token, Long userId, String email, String name) {
        return new LoginResponse(token, "admin", userId, email, name, null);
    }
}