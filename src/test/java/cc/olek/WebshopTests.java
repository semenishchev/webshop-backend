package cc.olek;

import cc.olek.webshop.user.User;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(DebugTestingProfile.class)
class WebshopTests {
    @Test
    void testLivenessProbe() {
        given()
          .when().get("/liveness")
          .then()
             .statusCode(200)
             .body(is("Alive!"));
    }

    @Test
    void testUserAuth() {
        String email = "test@example.com";
        String password = "12345";
        String loginData = """
            {
              "email": "%s",
              "password": "%s"
            }""".formatted(email, password);
        given()
            .when()
            .body(loginData)
            .contentType(ContentType.JSON)
            .post("/auth/register")
            .then()
            .statusCode(201);
        Response post = given()
            .when()
            .body(loginData)
            .contentType(ContentType.JSON)
            .post("/auth/login");
        String response = post.getBody().prettyPrint();
        post
            .then()
            .statusCode(200);
        System.out.println(response);
        String userInfo = given()
            .when()
            .header("Authorization", "Basic " + response)
            .get("/user/me")
            .prettyPrint();
        System.out.println(userInfo);
//            .as(User.class);
//        System.out.println(authorization);
//        assertEquals(email, authorization.email);
    }
}