package br.pucpr.agendafacil.app.security;

import br.pucpr.agendafacil.domain.identity.port.PasswordHashingService;
import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Serviço responsável por gerar e verificar hashes de senha com Argon2id.
 *
 * <p>Os parâmetros do algoritmo, como memória, iterações, paralelismo, tamanho
 * do salt e tamanho do hash, são carregados da configuração da aplicação. Isso
 * permite ajustar o custo do hash sem alterar o código.</p>
 *
 * <p>O hash é armazenado em formato PHC, incluindo os parâmetros usados,
 * o salt e o resultado do hash. Dessa forma, a verificação consegue recalcular
 * o valor usando os mesmos parâmetros gravados junto da senha.</p>
 */
@ApplicationScoped
public class PasswordHasher implements PasswordHashingService {

    @ConfigProperty(name = "agendafacil.security.argon2.memory-kib")
    int memoryKib;

    @ConfigProperty(name = "agendafacil.security.argon2.iterations")
    int iterations;

    @ConfigProperty(name = "agendafacil.security.argon2.parallelism")
    int parallelism;

    @ConfigProperty(name = "agendafacil.security.argon2.salt-bytes")
    int saltBytes;

    @ConfigProperty(name = "agendafacil.security.argon2.hash-bytes")
    int hashBytes;

    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getDecoder();

    /**
     * Gera o hash da senha informada.
     *
     * <p>Um novo salt é criado a cada chamada, evitando que senhas iguais gerem
     * o mesmo resultado armazenado.</p>
     *
     * @param plaintext senha em texto puro
     * @return hash codificado em formato PHC
     */
    @Override
    public String hash(String plaintext) {
        byte[] salt = new byte[saltBytes];
        RNG.nextBytes(salt);
        byte[] out = compute(plaintext, salt, iterations, memoryKib, parallelism, hashBytes);
        return String.format("$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                memoryKib, iterations, parallelism,
                B64.encodeToString(salt), B64.encodeToString(out));
    }

    /**
     * Verifica se a senha informada corresponde ao hash armazenado.
     *
     * <p>Os parâmetros são extraídos do próprio hash codificado. A comparação
     * final usa {@link MessageDigest#isEqual(byte[], byte[])} para evitar uma
     * comparação direta insegura entre arrays.</p>
     *
     * @param plaintext senha em texto puro
     * @param encoded hash armazenado
     * @return {@code true} quando a senha corresponder ao hash
     */
    @Override
    public boolean verify(String plaintext, String encoded) {
        Parsed p = parse(encoded);
        if (p == null) return false;
        byte[] candidate = compute(plaintext, p.salt, p.iterations, p.memoryKib, p.parallelism, p.hash.length);
        return MessageDigest.isEqual(candidate, p.hash);
    }

    /**
     * Calcula o hash Argon2id usando os parâmetros informados.
     */
    private static byte[] compute(String plaintext, byte[] salt, int iterations, int memoryKib,
                                  int parallelism, int hashLen) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryKib)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        byte[] out = new byte[hashLen];
        gen.generateBytes(plaintext.getBytes(StandardCharsets.UTF_8), out);
        return out;
    }

    /**
     * Lê um hash em formato PHC e extrai os parâmetros necessários para
     * verificação.
     *
     * @param encoded hash codificado
     * @return dados extraídos do hash ou {@code null} quando o formato for inválido
     */
    private static Parsed parse(String encoded) {
        if (encoded == null || !encoded.startsWith("$argon2id$")) return null;
        String[] parts = encoded.split("\\$");
        // "", "argon2id", "v=19", "m=...,t=...,p=...", "<salt>", "<hash>"
        if (parts.length != 6) return null;
        try {
            String[] kv = parts[3].split(",");
            int memoryKib = Integer.parseInt(kv[0].substring(2));
            int iterations = Integer.parseInt(kv[1].substring(2));
            int parallelism = Integer.parseInt(kv[2].substring(2));
            byte[] salt = B64D.decode(parts[4]);
            byte[] hash = B64D.decode(parts[5]);
            return new Parsed(memoryKib, iterations, parallelism, salt, hash);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Dados extraídos de um hash Argon2id codificado.
     */
    private record Parsed(int memoryKib, int iterations, int parallelism, byte[] salt, byte[] hash) {}
}