package org.techdive.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.techdive.dto.ComentarioRequest;
import org.techdive.dto.LoginRequest;
import org.techdive.dto.VideoRequest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComentariosTest {

    private ObjectMapper mapper = new ObjectMapper();

    private static String tokenJWT = null;
    private static String idVideo = null;
    private static Long idComentario = null;

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
    public void criarComentario() throws JsonProcessingException {
        ComentarioRequest request = new ComentarioRequest("mensagem de comentario");
        String json = mapper.writeValueAsString(request);
        Integer idComentarioAsInt = given()
                .header("Authorization", "Bearer " + tokenJWT)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/videos/{idVideo}/comentarios", idVideo)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("texto", is(request.getTexto()))
                .extract()
                .path("id");
        idComentario = idComentarioAsInt.longValue();
    }

    @Test
    @Order(4)
    public void apagarComentario() {
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .when()
                .delete("/videos/{idVideo}/comentarios/{idComentario}", idVideo, idComentario)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    public void apagarVideo() {
        given()
                .header("Authorization", "Bearer " + tokenJWT)
                .when()
                .delete("/videos/{id}", idVideo)
                .then()
                .statusCode(204);
    }

}
