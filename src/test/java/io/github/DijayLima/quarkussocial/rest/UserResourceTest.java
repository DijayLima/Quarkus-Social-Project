package io.github.DijayLima.quarkussocial.rest;

import io.github.DijayLima.quarkussocial.rest.dto.ResponseError;
import io.github.DijayLima.quarkussocial.rest.dto.UserRequest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("Should create an user successfully")
    @Order(1)
    public void createUserTest(){
        var user = new UserRequest();
        user.setName("Fulano");
        user.setAge(30);

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(user)
                .when()
                    .post(apiURL)
                .then()
                    .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @DisplayName("Should return error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest(){
        var user = new UserRequest();
        user.setAge(null);
        user.setName(null);


        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(user)
                .when()
                        .post(apiURL)
                .then()
                        .extract().response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));
        assertEquals("Age is Required", errors.get(0).get("message"));
        assertEquals("Name is Required", errors.get(1).get("message"));
    }

    @Test
    @DisplayName("Should list all users")
    @Order(3)
    public void listAllUsersTest(){
        given()
                .contentType(ContentType.JSON)
        .when()
                .get(apiURL)
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }
}