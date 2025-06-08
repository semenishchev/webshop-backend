package cc.olek;

import cc.olek.webshop.shop.model.Product;
import cc.olek.webshop.shop.model.ProductCategory;
import cc.olek.webshop.shop.model.ProductSorting;
import cc.olek.webshop.shop.service.ProductService;
import cc.olek.webshop.user.User;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.hibernate.search.mapper.orm.Search;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

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
            .statusCode(204);
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
            .header("Authorization", "Bearer " + response)
            .get("/user/me")
            .prettyPrint();
        assertTrue(userInfo.contains(email));
    }

    @Inject
    ProductService productService;

    @Test
    public void testProducts() {
        ProductCategory category = new ProductCategory();
        category.name = "Phones";
        productService.registerCategory(category);
        ProductCategory phones = productService.getCategory("Phones");
        assertNotNull(phones);
        assertEquals(phones.name, category.name);
        Product product = new Product();
        product.price = 500;
        product.name = "iPhone 16 Pro Max";
        product.description = "iPhone 16 Pro Max 512GB 48mp";
        product.stock = 100;
        product.timesBought = 4123;
        product.category = phones;
        productService.registerNewProduct(product);
        Search.session(Product.getEntityManager()).workspace().flush();
        Search.session(Product.getEntityManager()).workspace().refresh();
        String[] variations = {"phine", "pone", "phone", "iphone", "phone 512"};
        for (String variation : variations) {
            assertFalse(productService.autocomplete(variation).isEmpty());
            assertFalse(productService.search(null, ProductSorting.RELEVANCE, 0, 5, variation).isEmpty());
            Log.infof("Passed %s", variation);
        }
    }
}