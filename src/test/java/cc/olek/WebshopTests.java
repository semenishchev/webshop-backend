package cc.olek;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

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

}