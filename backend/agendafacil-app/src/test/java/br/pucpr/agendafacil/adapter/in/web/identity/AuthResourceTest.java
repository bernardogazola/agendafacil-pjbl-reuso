package br.pucpr.agendafacil.adapter.in.web.identity;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    @Test
    void customerSignup_thenLogin_returnsToken() {
        String email = "cliente-" + UUID.randomUUID() + "@teste.com";

        given().contentType("application/json")
                .body(Map.of("name", "Ana Cliente", "email", email, "password", "senha1234"))
                .when().post("/api/v1/auth/customer/signup")
                .then().statusCode(201)
                .body("token", notNullValue())
                .body("role", equalTo("customer"));

        given().contentType("application/json")
                .body(Map.of("email", email, "password", "senha1234"))
                .when().post("/api/v1/auth/login")
                .then().statusCode(200)
                .body("role", equalTo("customer"))
                .body("token", notNullValue());
    }

    @Test
    void duplicateEmail_returns409() {
        String email = "dup-" + UUID.randomUUID() + "@teste.com";
        given().contentType("application/json")
                .body(Map.of("name", "Ana", "email", email, "password", "senha1234"))
                .when().post("/api/v1/auth/customer/signup")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(Map.of("name", "Ana", "email", email, "password", "senha1234"))
                .when().post("/api/v1/auth/customer/signup")
                .then().statusCode(409);
    }

    @Test
    void loginWithWrongPassword_returns401() {
        String email = "wrong-" + UUID.randomUUID() + "@teste.com";
        given().contentType("application/json")
                .body(Map.of("name", "Ana", "email", email, "password", "senha1234"))
                .when().post("/api/v1/auth/customer/signup")
                .then().statusCode(201);

        given().contentType("application/json")
                .body(Map.of("email", email, "password", "errada"))
                .when().post("/api/v1/auth/login")
                .then().statusCode(401);
    }

    @Test
    void ownerSignup_returnsOwnerRoleAndBusinessId() {
        String email = "dono-" + UUID.randomUUID() + "@teste.com";
        given().contentType("application/json")
                .body(Map.of(
                        "ownerName", "Dono",
                        "ownerEmail", email,
                        "ownerPassword", "senha1234",
                        "businessTradeName", "Salão X",
                        "businessEmail", "salao-" + UUID.randomUUID() + "@teste.com",
                        "category", "SALON",
                        "plan", "BASIC"))
                .when().post("/api/v1/auth/owner/signup")
                .then().statusCode(201)
                .body("role", equalTo("owner"))
                .body("businessId", notNullValue());
    }
}