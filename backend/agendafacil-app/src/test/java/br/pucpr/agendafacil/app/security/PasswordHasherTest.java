package br.pucpr.agendafacil.app.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PasswordHasherTest {

    @Inject
    PasswordHasher hasher;

    @Test
    void hash_andVerify_roundTrip() {
        String encoded = hasher.hash("senha-super-secreta");

        assertTrue(encoded.startsWith("$argon2id$v=19$m=65536,t=3,p=1$"),
                "formato PHC esperado; foi: " + encoded);
        assertTrue(hasher.verify("senha-super-secreta", encoded),
                "senha correta deveria validar");
    }

    @Test
    void verify_rejectsWrongPassword() {
        String encoded = hasher.hash("senha1");

        assertFalse(hasher.verify("senha2", encoded), "senha errada não pode validar");
    }

    @Test
    void hash_isDifferentForSamePlaintext_dueToSalt() {
        String first = hasher.hash("mesmasenha");
        String second = hasher.hash("mesmasenha");

        assertNotEquals(first, second,
                "Dois hashes da mesma senha devem diferir por causa do salt aleatório");
        assertTrue(hasher.verify("mesmasenha", first));
        assertTrue(hasher.verify("mesmasenha", second));
    }

    @Test
    void verify_returnsFalse_onMalformedEncoded() {
        assertFalse(hasher.verify("qualquercoisa", "não-é-formato-argon2id"));
        assertFalse(hasher.verify("qualquercoisa", ""));
    }
}