package br.pucpr.agendafacil.adapter.in.web.notification;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.empty;

/**
 * Testes de integração dos endpoints de preferências de notificação.
 *
 * <p>Valida o canal padrão criado no cadastro do cliente, a atualização das
 * preferências, o opt-out por lista vazia e as regras de autenticação e papel.</p>
 */
@QuarkusTest
class NotificationPreferencesResourceTest {

    @Test
    void getWithoutToken_returns401() {
        given()
                .when().get("/api/v1/customers/me/notification-preferences")
                .then().statusCode(401);
    }

    @Test
    void newCustomer_defaultsToEmailChannel() {
        String token = signupCustomer();

        given().auth().oauth2(token)
                .when().get("/api/v1/customers/me/notification-preferences")
                .then().statusCode(200)
                .body("channels", hasSize(1))
                .body("channels", containsInAnyOrder("EMAIL"));
    }

    @Test
    void put_replacesChannelSet() {
        String token = signupCustomer();

        given().auth().oauth2(token).contentType("application/json")
                .body(Map.of("channels", List.of("EMAIL", "WHATSAPP")))
                .when().put("/api/v1/customers/me/notification-preferences")
                .then().statusCode(200)
                .body("channels", hasSize(2))
                .body("channels", containsInAnyOrder("EMAIL", "WHATSAPP"));

        given().auth().oauth2(token)
                .when().get("/api/v1/customers/me/notification-preferences")
                .then().statusCode(200)
                .body("channels", containsInAnyOrder("EMAIL", "WHATSAPP"));
    }

    @Test
    void putEmptyArray_optsOutCompletely() {
        String token = signupCustomer();

        given().auth().oauth2(token).contentType("application/json")
                .body(Map.of("channels", List.of()))
                .when().put("/api/v1/customers/me/notification-preferences")
                .then().statusCode(200)
                .body("channels", empty());

        given().auth().oauth2(token)
                .when().get("/api/v1/customers/me/notification-preferences")
                .then().statusCode(200)
                .body("channels", empty());
    }

    @Test
    void putWithMissingChannels_returns400() {
        String token = signupCustomer();

        given().auth().oauth2(token).contentType("application/json")
                .body("{}")
                .when().put("/api/v1/customers/me/notification-preferences")
                .then().statusCode(400);
    }

    @Test
    void ownerToken_isForbidden() {
        String ownerEmail = "owner-pref-" + UUID.randomUUID() + "@teste.com";
        String ownerToken = given().contentType("application/json")
                .body(Map.of(
                        "ownerName", "Dono Pref",
                        "ownerEmail", ownerEmail,
                        "ownerPassword", "senha1234",
                        "businessTradeName", "Negócio Pref",
                        "businessEmail", "biz-pref-" + UUID.randomUUID() + "@teste.com",
                        "category", "BARBER_SHOP",
                        "plan", "BASIC"))
                .when().post("/api/v1/auth/owner/signup")
                .then().statusCode(201)
                .extract().jsonPath().getString("token");

        given().auth().oauth2(ownerToken)
                .when().get("/api/v1/customers/me/notification-preferences")
                .then().statusCode(403);
    }

    private static String signupCustomer() {
        String email = "pref-" + UUID.randomUUID() + "@teste.com";
        return given().contentType("application/json")
                .body(Map.of(
                        "name", "Cliente Pref",
                        "email", email,
                        "password", "senha1234"))
                .when().post("/api/v1/auth/customer/signup")
                .then().statusCode(201)
                .extract().jsonPath().getString("token");
    }
}