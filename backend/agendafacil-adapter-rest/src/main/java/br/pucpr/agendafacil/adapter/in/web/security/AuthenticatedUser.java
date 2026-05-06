package br.pucpr.agendafacil.adapter.in.web.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Representa o usuário autenticado da requisição atual.
 *
 * <p>Esta classe encapsula o acesso ao {@link JsonWebToken} e expõe apenas os
 * dados que os resources precisam usar, como id, e-mail e papel do usuário.</p>
 */
@RequestScoped
public class AuthenticatedUser {

    private final JsonWebToken jwt;

    @Inject
    public AuthenticatedUser(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    /**
     * Retorna o identificador do usuário autenticado.
     *
     * <p>O valor é lido do claim {@code sub} do token.</p>
     *
     * @return id do usuário autenticado ou {@code null} quando o claim não existir
     */
    public Long id() {
        String sub = jwt.getSubject();
        return sub == null ? null : Long.valueOf(sub);
    }

    /**
     * Retorna o e-mail do usuário autenticado.
     *
     * @return e-mail associado ao token
     */
    public String email() {
        return jwt.getName();
    }

    /**
     * Retorna o papel do usuário autenticado na aplicação.
     *
     * <p>O valor é lido do claim customizado {@code role}.</p>
     *
     * @return papel do usuário, como {@code customer} ou {@code owner}
     */
    public String role() {
        return jwt.getClaim("role");
    }
}