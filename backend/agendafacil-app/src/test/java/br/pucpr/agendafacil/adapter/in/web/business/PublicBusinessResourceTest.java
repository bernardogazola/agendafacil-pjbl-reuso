package br.pucpr.agendafacil.adapter.in.web.business;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * Testes de integração dos endpoints públicos de estabelecimentos.
 *
 * <p>Valida a listagem de estabelecimentos ativos e a consulta de serviços
 * ativos de um estabelecimento.</p>
 */
@QuarkusTest
class PublicBusinessResourceTest {

    @Test
    void listActive_returnsAtLeastTheBusinessWeJustCreated() {
        String ownerEmail = "pub-" + UUID.randomUUID() + "@teste.com";
        String bizEmail = "pub-biz-" + UUID.randomUUID() + "@teste.com";
        String bizName = "Barbearia " + UUID.randomUUID();

        given().contentType("application/json")
                .body(Map.of(
                        "ownerName", "Dono Público",
                        "ownerEmail", ownerEmail,
                        "ownerPassword", "senha1234",
                        "businessTradeName", bizName,
                        "businessEmail", bizEmail,
                        "category", "BARBER_SHOP",
                        "plan", "BASIC"))
                .when().post("/api/v1/auth/owner/signup")
                .then().statusCode(201);

        given()
                .when().get("/api/v1/businesses")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("tradeName", hasItem(equalTo(bizName)))
                .body("active", not(hasItem(is(false))));
    }

    @Test
    void listServices_unknownBusiness_returns404() {
        given()
                .when().get("/api/v1/businesses/999999999/services")
                .then().statusCode(404)
                .body("error", equalTo("Estabelecimento não encontrado."));
    }

    @Test
    void listServices_emptyWhenBusinessHasNoServices() {
        String ownerEmail = "pub-empty-" + UUID.randomUUID() + "@teste.com";
        String bizEmail = "pub-empty-biz-" + UUID.randomUUID() + "@teste.com";

        long businessId = given().contentType("application/json")
                .body(Map.of(
                        "ownerName", "Dono",
                        "ownerEmail", ownerEmail,
                        "ownerPassword", "senha1234",
                        "businessTradeName", "Sem Serviços",
                        "businessEmail", bizEmail,
                        "category", "SALON",
                        "plan", "BASIC"))
                .when().post("/api/v1/auth/owner/signup")
                .then().statusCode(201)
                .extract().jsonPath().getLong("businessId");

        given()
                .when().get("/api/v1/businesses/" + businessId + "/services")
                .then().statusCode(200)
                .body("$", empty());
    }

    @Test
    void listServices_returnsOnlyActiveServicesForBusiness() {
        String ownerEmail = "pub-svc-" + UUID.randomUUID() + "@teste.com";
        String bizEmail = "pub-svc-biz-" + UUID.randomUUID() + "@teste.com";

        var ownerResp = given().contentType("application/json")
                .body(Map.of(
                        "ownerName", "Dono",
                        "ownerEmail", ownerEmail,
                        "ownerPassword", "senha1234",
                        "businessTradeName", "Com Serviço",
                        "businessEmail", bizEmail,
                        "category", "CLINIC",
                        "plan", "BASIC"))
                .when().post("/api/v1/auth/owner/signup")
                .then().statusCode(201)
                .extract().jsonPath();
        String ownerToken = ownerResp.getString("token");
        long businessId = ownerResp.getLong("businessId");

        given().auth().oauth2(ownerToken)
                .contentType("application/json")
                .body(Map.of(
                        "name", "Consulta padrão",
                        "basePrice", 150.00,
                        "durationMinutes", 30))
                .when().post("/api/v1/businesses/" + businessId + "/services")
                .then().statusCode(201);

        given()
                .when().get("/api/v1/businesses/" + businessId + "/services")
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].name", equalTo("Consulta padrão"))
                .body("[0].active", equalTo(true));
    }
}