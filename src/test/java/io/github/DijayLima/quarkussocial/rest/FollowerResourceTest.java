package io.github.DijayLima.quarkussocial.rest;

import io.github.DijayLima.quarkussocial.domain.model.Follower;
import io.github.DijayLima.quarkussocial.domain.model.User;
import io.github.DijayLima.quarkussocial.domain.repository.FollowerRepository;
import io.github.DijayLima.quarkussocial.domain.repository.UserRepository;
import io.github.DijayLima.quarkussocial.rest.dto.FollowerRequest;
import io.github.DijayLima.quarkussocial.rest.dto.PostRequest;
import io.github.DijayLima.quarkussocial.rest.dto.UserRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        //Usuário padrão dos testes
        var user =  new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //Seguidor
        var follower =  new User();
        follower.setAge(31);
        follower.setName("Cicrano");
        userRepository.persist(follower);
        followerId = follower.getId();

        //cria um follower
        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("Should return 409 when followerId is equal to User id")
    public void sameUserAsFollowerTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can\'t follow yourself!"));

    }

    @Test
    @DisplayName("Should return 404 on follow a user when User doesn't exist")
    public void userNotFoundWhenTryingToFollowTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", inexistentUserId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should follow a user")
    public void followUserTest(){
        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("Should return 404 on list user followers and User doesn't exist")
    public void userNotFoundWhenListingToFollowersTest(){

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", inexistentUserId)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should list a user's followers")
    public void listFollowersTest(){
        var response = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
        .when()
                .get()
        .then()
                .extract().response();

        var followerCount = response.jsonPath().get("followersCount");
        var followerList = response.jsonPath().getList("content");
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followerCount);
        assertEquals(1, followerList.size());
    }

    @Test
    @DisplayName("Should return 404 on unfollow user and user followers and User doesn't exist")
    public void userNotFoundWhenUnfollowingAUserTest(){

        var inexistentUserId = 999;

        given()
                .pathParam("userId", inexistentUserId)
                .queryParam("followerId", followerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should Unfollow an user")
    public void unfollowingUserTest(){
        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}