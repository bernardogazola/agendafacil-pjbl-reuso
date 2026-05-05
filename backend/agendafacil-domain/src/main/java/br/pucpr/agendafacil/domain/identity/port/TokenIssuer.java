package br.pucpr.agendafacil.domain.identity.port;

import br.pucpr.agendafacil.domain.identity.User;

/**
 * Serviço responsável por emitir tokens de autenticação.
 *
 * <p>Esta interface funciona como uma porta de saída da aplicação. O formato do
 * token, como JWT, sessão opaca ou outro mecanismo, fica como detalhe da
 * infraestrutura.</p>
 *
 * <p>A camada de aplicação apenas solicita a emissão do token para um usuário
 * autenticado e devolve essa informação no fluxo de login.</p>
 */
public interface TokenIssuer {

    /**
     * Emite um token de autenticação para o usuário informado.
     *
     * @param user usuário autenticado
     * @param role papel usado na autorização do usuário
     * @return token emitido
     */
    String issue(User user, String role);
}