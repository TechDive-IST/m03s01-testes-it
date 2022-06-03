package org.techdive.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.techdive.dto.LoginRequest;
import org.techdive.dto.VideoRequest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VideosTest {

    private ObjectMapper mapper = new ObjectMapper();

    private static String tokenJWT = null;
    private static String idVideo = null;

    @BeforeAll
    public static void preCondicao() {
        baseURI = "http://localhost";
        port = 8080;
        basePath = "/m03s01-1.0-SNAPSHOT/api";
    }

    @Test
    @Order(1)
    public void autenticacao() throws JsonProcessingException {
        LoginRequest request = new LoginRequest("james@kirk.com", "1234");
        String json = mapper.writeValueAsString(request);
        tokenJWT = given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/login")
                .then()
                .statusCode(201)
                .extract()
                .asString();
    }

    @Test
    @Order(2)
    public void criarVideo() throws JsonProcessingException {
        VideoRequest request = new VideoRequest("http://blablabla.com", "assunto", "usuario", 30);
        String json = mapper.writeValueAsString(request);
        idVideo = given()
                .header("Authorization", "Bearer " + tokenJWT)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/videos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Test
    @Order(3)
    public void alterarVideo() throws JsonProcessingException {
        VideoRequest request = new VideoRequest("http://blablabla.com", "assunto_alterado", "usuario_alterado", 30);
        String json = mapper.writeValueAsString(request);
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/videos/{id}", idVideo)
                .then()
                .statusCode(200)
                .body("assunto", is("assunto_alterado"))
                .body("usuario", is("usuario_alterado"))
                .body("url", is(request.getUrl()))
                .body("id", notNullValue());
    }

    @Test
    @Order(4)
    public void listarVideos() {
        given()
                .when()
                .get("/videos")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @Order(5)
    public void listarVideoPorId() {
        given()
                .when()
                .get("/videos/{id}", idVideo)
                .then()
                .statusCode(200)
                .body("usuario", is("usuario_alterado"))
                .body("assunto", is("assunto_alterado"));
    }

    @Test
    @Order(6)
    public void darLikeNoVideo() {
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .when()
                .put("/videos/{id}/like", idVideo)
                .then()
                .statusCode(200)
                .body("id", is(idVideo))
                .body("likes", is(1));
    }

    @Test
    @Order(7)
    public void removerLikeNoVideo() {
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .when()
                .delete("/videos/{id}/like", idVideo)
                .then()
                .statusCode(200)
                .body("id", is(idVideo))
                .body("likes", is(0));
    }

    @Test
    @Order(8)
    public void visualizarVideo() {
        given()
                .when()
                .get("/videos/{id}/visualizacao", idVideo)
                .then()
                .statusCode(200)
                .body("id", is(idVideo))
                .body("visualizacoes", is(1));
    }

    @Test
    @Order(9)
    public void apagarVideo() {
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .when()
                .delete("/videos/{id}", idVideo)
                .then()
                .statusCode(204);
    }

}
