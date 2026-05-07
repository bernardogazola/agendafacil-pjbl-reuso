package br.pucpr.agendafacil.app.security;

import br.pucpr.agendafacil.domain.identity.User;
import br.pucpr.agendafacil.domain.identity.port.TokenIssuer;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

/**
 * Serviço responsável por emitir tokens JWT para usuários autenticados.
 *
 * <p>O token inclui os dados necessários para identificação e autorização do
 * usuário na API, como e-mail, id, nome e papel. O papel também é enviado em
 * {@code groups}, pois esse claim é usado pelo MP-JWT nas validações com
 * {@code @RolesAllowed}.</p>
 *
 * <p>O tempo de expiração é configurado por
 * {@code smallrye.jwt.new-token.lifespan}, em segundos.</p>
 */
@ApplicationScoped
public class JwtService implements TokenIssuer {

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "3600")
    long lifespanSeconds;

    /**
     * Emite um token de autenticação para o usuário informado.
     *
     * @param user usuário autenticado
     * @param role papel usado na autorização do usuário
     * @return token JWT assinado
     */
    @Override
    public String issue(User user, String role) {
        return Jwt.upn(user.getEmail())
                .subject(String.valueOf(user.getId()))
                .groups(Set.of(role))
                .claim("role", role)
                .claim("name", user.getName())
                .expiresIn(Duration.ofSeconds(lifespanSeconds))
                .sign();
    }
}