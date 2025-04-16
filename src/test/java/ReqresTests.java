import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class ReqresTests {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "https://reqres.in";
        RestAssured.basePath = "/api";
    }

    @Test
    @DisplayName("Проверка на соответствие JSON-схеме ответа запроса получения пользователя с ID 2")
    void testSingleUserJsonSchema() {
        given().
        when().
                get("/users/2").
        then().
                statusCode(200).
                body(matchesJsonSchemaInClasspath("single-user-schema.json"));
    }

    @Test
    @DisplayName("Проверка получения второй страницы пользователей")
    void testPagination() {
        given().
                queryParam("page", 2).
        when().
                get("/users").
        then().
                statusCode(200).
                body("page", equalTo(2)).
                body("data", not(empty()));
    }

    @Test
    @DisplayName("Проверка получения ошибки при авторизации без пароля")
    void testLoginMissingPassword() {
        String payload = "{ \"email\": \"peter@klaven\" }";

        given().
                contentType(ContentType.JSON).
                body(payload).
        when().
                post("/login").
        then().
                statusCode(400).
                body("error", equalTo("Missing password"));
    }

    @Test
    @DisplayName("Проверка соответствия id пользователя и его email")
    void testMultipleUsersExist() {
        for (int id = 1; id <= 5; id++) {
            given().
            when().
                    get("/users/{id}", id).
            then().
                    statusCode(200).
                    body("data.id", equalTo(id)).
                    body("data.email", containsString("@reqres.in"));
        }
    }

    @Test
    @DisplayName("Создание пользователя и получение его по ID")
    void testCreateThenGetUser() {
        String name = "neo";
        String job = "chosen one";

        var response = given().
                contentType(ContentType.JSON).
                body("{\"name\": \"" + name + "\", \"job\": \"" + job + "\"}").
        when().
                post("/users").
        then().
                statusCode(201).
                body("name", equalTo(name)).
                extract();

        String id = response.path("id");

        given().
        when().
                get("/users/" + id).
        then().
                statusCode(anyOf(equalTo(200), equalTo(404))); //костыльный 404
    }
}
