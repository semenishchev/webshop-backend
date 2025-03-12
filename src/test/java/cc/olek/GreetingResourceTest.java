package cc.olek;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testLivenessProbe() {
        given()
          .when().get("/liveness")
          .then()
             .statusCode(200)
             .body(is("Alive"));
    }

}