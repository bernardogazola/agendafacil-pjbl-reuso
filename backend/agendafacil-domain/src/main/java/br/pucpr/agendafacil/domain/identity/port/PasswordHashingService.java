package br.pucpr.agendafacil.domain.identity.port;

/**
 * Serviço responsável por gerar e verificar hashes de senha.
 *
 * <p>Esta interface funciona como uma porta de saída da aplicação. O algoritmo
 * usado para gerar o hash, como Argon2id, bcrypt ou outro, fica como detalhe da
 * infraestrutura.</p>
 *
 * <p>A aplicação depende apenas das operações de gerar o hash de uma senha e
 * verificar se uma senha informada corresponde a um hash já salvo.</p>
 */
public interface PasswordHashingService {

    /**
     * Gera o hash da senha informada.
     *
     * @param plaintext senha em texto puro
     * @return hash gerado para armazenamento
     */
    String hash(String plaintext);

    /**
     * Verifica se a senha informada corresponde ao hash armazenado.
     *
     * @param plaintext senha em texto puro
     * @param encoded hash armazenado
     * @return {@code true} se a senha corresponder ao hash
     */
    boolean verify(String plaintext, String encoded);
}