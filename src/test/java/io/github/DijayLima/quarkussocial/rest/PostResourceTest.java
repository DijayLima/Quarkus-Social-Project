package io.github.DijayLima.quarkussocial.rest;

import io.github.DijayLima.quarkussocial.domain.model.Follower;
import io.github.DijayLima.quarkussocial.domain.model.Post;
import io.github.DijayLima.quarkussocial.domain.model.User;
import io.github.DijayLima.quarkussocial.domain.repository.FollowerRepository;
import io.github.DijayLima.quarkussocial.domain.repository.PostRepository;
import io.github.DijayLima.quarkussocial.domain.repository.UserRepository;
import io.github.DijayLima.quarkussocial.rest.dto.PostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUP(){
        //Usuário padrão dos testes
        var user =  new User();
        user.setAge(30);
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //Criada a postagem para o usuário padrão
        Post post = new Post();
        post.setText("Hello!");
        post.setUser(user);
        postRepository.persist(post);

        //Usuário que não segue
        var userNotFollower =  new User();
        userNotFollower.setAge(31);
        userNotFollower.setName("Seguidor");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //Usuário seguidor
        var userFollower =  new User();
        userFollower.setAge(33);
        userFollower.setName("Cicrano");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);


    }

    @Test
    @DisplayName("Should create an post for a user")
    public void createPostTest(){
        var postResquest = new PostRequest();
        postResquest.setText("Some text");

        given()
                .contentType(ContentType.JSON)
                .body(postResquest)
                .pathParam("userId", userId)
        .when()
                .post()
        .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Should return 404 when trying to make a post for an inexistent user")
    public void postForAnInexistentUserTest(){
        var postResquest = new PostRequest();
        postResquest.setText("Some text");

        var inexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postResquest)
                .pathParam("userId", inexistentUserId)
        .when()
                .post()
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest(){
        var inexistentUserId = 999;

        given()
                .pathParam("userId", inexistentUserId)
        .when()
                .get()
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest(){
        given()
                .pathParam("userId", userId)
        .when()
                .get()
        .then()
                .statusCode(400)
                .body(Matchers.is("You forget the header followerId"));
    }

    @Test
    @DisplayName("Should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){

        var inexistentFollowerId = 999;

        given()
                .pathParam("userId", userId)
                .header("followerId", inexistentFollowerId)
        .when()
                .get()
        .then()
                .statusCode(400)
                .body(Matchers.is("Inexistent followerId"));
    }

    @Test
    @DisplayName("Should return 403 when follower isn't a follower")
    public void listPostNotAFollowerTest(){
        given()
                .pathParam("userId", userId)
                .header("followerId", userNotFollowerId)
        .when()
                .get()
        .then()
                .statusCode(403)
                .body(Matchers.is("You can't see these posts"));
    }

    @Test
    @DisplayName("Should return posts")
    public void listPostsTest(){
        given()
                .pathParam("userId", userId)
                .header("followerId", userFollowerId)
        .when()
                .get()
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

}