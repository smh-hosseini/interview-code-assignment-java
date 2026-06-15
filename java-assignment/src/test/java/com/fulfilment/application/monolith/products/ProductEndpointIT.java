package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class ProductEndpointIT {

  private static final String BASE_PATH = "/product";

  @Test
  public void testListAllProducts() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("name", hasItems("TONSTAD", "KALLAX", "BESTÅ"));
  }



  @Test
  public void testGetProductById_Success() {
    given()
        .when()
        .get(BASE_PATH + "/1")
        .then()
        .statusCode(200)
        .body("name", equalTo("TONSTAD"))
        .body("stock", equalTo(10));
  }

  @Test
  public void testGetProductById_FailWhenNotFound() {
    given()
        .when()
        .get(BASE_PATH + "/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testCreateProduct_Success() {
    String requestBody =
        """
        {
          "name": "EKTORP",
          "description": "Three-seat sofa",
          "price": 599.99,
          "stock": 15
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("name", equalTo("EKTORP"))
        .body("description", equalTo("Three-seat sofa"))
        .body("price", equalTo(599.99f))
        .body("stock", equalTo(15));

    // Verify it was created
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("name", hasItem("EKTORP"));
  }

  @Test
  public void testCreateProduct_WithMinimalData() {
    String requestBody =
        """
        {
          "name": "LACK",
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("name", equalTo("LACK"))
        .body("description", nullValue())
        .body("price", nullValue())
        .body("stock", equalTo(0));
  }

  @Test
  public void testCreateProduct_FailWhenIdIsSet() {
    String requestBody =
        """
        {
          "id": 100,
          "name": "INVALID",
          "stock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("Id was invalidly set"))
        .body("errorCode", equalTo("PRODUCT_VALIDATION_ERROR"));
  }

  @Test
  public void testCreateProduct_FailWhenDuplicateName() {
    String requestBody =
        """
        {
          "name": "TONSTAD",
          "stock": 5
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(500); // Database constraint violation
  }

  @Test
  public void testUpdateProduct_Success() {
    // First create a product
    String createRequest =
        """
        {
          "name": "BILLY",
          "description": "Bookcase",
          "price": 49.99,
          "stock": 20
        }
        """;

    var createResponse = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract();

    Integer productId = createResponse.path("id");

    // Update it
    String updateRequest =
        """
        {
          "name": "BILLY-UPDATED",
          "description": "Bookcase - White",
          "price": 59.99,
          "stock": 25
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("name", equalTo("BILLY-UPDATED"))
        .body("description", equalTo("Bookcase - White"))
        .body("price", equalTo(59.99f))
        .body("stock", equalTo(25));
  }

  @Test
  public void testUpdateProduct_FailWhenNotFound() {
    String updateRequest =
        """
        {
          "name": "NONEXISTENT",
          "stock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testUpdateProduct_FailWhenNameIsNull() {
    String updateRequest =
        """
        {
          "stock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/1")
        .then()
        .statusCode(400)
        .body("message", containsString("Product Name was not set"))
        .body("errorCode", equalTo("PRODUCT_VALIDATION_ERROR"));
  }

  @Test
  public void testUpdateProduct_AllowNullDescription() {
    // First create a product with description
    String createRequest =
        """
        {
          "name": "HEMNES",
          "description": "Bed frame",
          "price": 299.99,
          "stock": 10
        }
        """;

    var createResponse = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract();

    Integer productId = createResponse.path("id");

    // Update to remove description
    String updateRequest =
        """
        {
          "name": "HEMNES",
          "description": null,
          "price": 299.99,
          "stock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("description", nullValue());
  }

  @Test
  public void testUpdateProduct_AllowNullPrice() {
    // First create a product with price
    String createRequest =
        """
        {
          "name": "MALM",
          "price": 199.99,
          "stock": 8
        }
        """;

    var createResponse = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract();

    Integer productId = createResponse.path("id");

    // Update to remove price
    String updateRequest =
        """
        {
          "name": "MALM",
          "price": null,
          "stock": 8
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("price", nullValue());
  }

  @Test
  public void testDeleteProduct_Success() {
    // First create a product
    String createRequest =
        """
        {
          "name": "POÄNG",
          "description": "Armchair",
          "price": 79.99,
          "stock": 12
        }
        """;

    var createResponse = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract();

    Integer productId = createResponse.path("id");

    // Delete it
    given()
        .when()
        .delete(BASE_PATH + "/" + productId)
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get(BASE_PATH + "/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteProduct_FailWhenNotFound() {
    given()
        .when()
        .delete(BASE_PATH + "/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testCreateProduct_WithPrecisePrice() {
    String requestBody =
        """
        {
          "name": "PAX",
          "description": "Wardrobe frame",
          "price": 123.45,
          "stock": 7
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("price", equalTo(123.45f));
  }

  @Test
  public void testUpdateProduct_UpdateOnlyStock() {
    // First create a product
    String createRequest =
        """
        {
          "name": "KALLAX-SHELF",
          "description": "Shelving unit",
          "price": 89.99,
          "stock": 30
        }
        """;

    var createResponse = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract();

    Integer productId = createResponse.path("id");

    // Update only stock
    String updateRequest =
        """
        {
          "name": "KALLAX-SHELF",
          "description": "Shelving unit",
          "price": 89.99,
          "stock": 35
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("stock", equalTo(35))
        .body("name", equalTo("KALLAX-SHELF"))
        .body("description", equalTo("Shelving unit"))
        .body("price", equalTo(89.99f));
  }

  @Test
  public void testCreateProduct_WithZeroStock() {
    String requestBody =
        """
        {
          "name": "STUVA",
          "description": "Storage combination",
          "price": 299.00,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("stock", equalTo(0));
  }

  @Test
  public void testProductsSortedByName() {
    // Create multiple products
    String product1 =
        """
        {
          "name": "ZEBRA",
          "stock": 5
        }
        """;

    String product2 =
        """
        {
          "name": "ALPHA",
          "stock": 5
        }
        """;

    given().contentType(ContentType.JSON).body(product1).post(BASE_PATH);
    given().contentType(ContentType.JSON).body(product2).post(BASE_PATH);

    // Verify they are sorted by name
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("name[0]", equalTo("ALPHA")) // First alphabetically
        .body("name", hasItem("ZEBRA"));
  }
}
