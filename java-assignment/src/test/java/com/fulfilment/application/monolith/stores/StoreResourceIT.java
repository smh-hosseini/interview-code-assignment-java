package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceIT {

  private static final String BASE_PATH = "/store";

  @Test
  public void testListAllStores() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  @Test
  public void testGetStoreByName_Success() {
    // TONSTAD already exists from import.sql with quantity 10
    given()
        .when()
        .get(BASE_PATH + "/TONSTAD")
        .then()
        .statusCode(200)
        .body("name", equalTo("TONSTAD"))
        .body("quantityProductsInStock", equalTo(10));
  }

  @Test
  public void testGetStoreByName_FailWhenNotFound() {
    given()
        .when()
        .get(BASE_PATH + "/NONEXISTENT")
        .then()
        .statusCode(404);
  }

  @Test
  public void testCreateStore_Success() {
    String requestBody =
        """
        {
          "name": "HEMNES",
          "quantityProductsInStock": 25
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Verify it was created
    given()
        .when()
        .get(BASE_PATH + "/HEMNES")
        .then()
        .statusCode(200)
        .body("name", equalTo("HEMNES"))
        .body("quantityProductsInStock", equalTo(25));
  }

  @Test
  public void testCreateStore_WithZeroStock() {
    String requestBody =
        """
        {
          "name": "BILLY",
          "quantityProductsInStock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);
  }

  @Test
  public void testCreateStore_FailWhenNameIsNull() {
    String requestBody =
        """
        {
          "quantityProductsInStock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("name is required"));
  }

  @Test
  public void testCreateStore_FailWhenNameIsBlank() {
    String requestBody =
        """
        {
          "name": "   ",
          "quantityProductsInStock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("name"));
  }

  @Test
  public void testCreateStore_FailWhenNameExceeds40Characters() {
    String requestBody =
        """
        {
          "name": "VERYLONGSTORENAME12345678901234567890123456789",
          "quantityProductsInStock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("40 characters"));
  }

  @Test
  public void testCreateStore_FailWhenQuantityIsNegative() {
    String requestBody =
        """
        {
          "name": "MALM",
          "quantityProductsInStock": -5
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("cannot be negative"));
  }

  @Test
  public void testCreateStore_FailWhenDuplicateName() {
    String createRequest =
        """
        {
          "name": "DUPLICATE",
          "quantityProductsInStock": 15
        }
        """;

    // Create first time - should succeed
    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Create again with same name - should fail
    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  public void testUpdateStore_Success() {
    // First create a store
    String createRequest =
        """
        {
          "name": "PAX",
          "quantityProductsInStock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Update it (full replacement)
    String updateRequest =
        """
        {
          "name": "PAX",
          "quantityProductsInStock": 35
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/PAX")
        .then()
        .statusCode(204);

    // Verify it was updated
    given()
        .when()
        .get(BASE_PATH + "/PAX")
        .then()
        .statusCode(200)
        .body("name", equalTo("PAX"))
        .body("quantityProductsInStock", equalTo(35));
  }

  @Test
  public void testUpdateStore_FailWhenNotFound() {
    String updateRequest =
        """
        {
          "name": "NONEXISTENT",
          "quantityProductsInStock": 20
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/NONEXISTENT")
        .then()
        .statusCode(404);
  }

  @Test
  public void testUpdateStore_ReplacesAllFields() {
    // Create store with stock
    String createRequest =
        """
        {
          "name": "LISABO",
          "quantityProductsInStock": 100
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Update with new quantity
    String updateRequest =
        """
        {
          "name": "LISABO",
          "quantityProductsInStock": 50
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/LISABO")
        .then()
        .statusCode(204);

    // Verify full replacement happened
    given()
        .when()
        .get(BASE_PATH + "/LISABO")
        .then()
        .statusCode(200)
        .body("name", equalTo("LISABO"))
        .body("quantityProductsInStock", equalTo(50));
  }

  @Test
  public void testPatchStore_UpdateQuantityOnly() {
    // First create a store
    String createRequest =
        """
        {
          "name": "IVAR",
          "quantityProductsInStock": 15
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Patch only the quantity
    String patchRequest =
        """
        {
          "quantityProductsInStock": 25
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/IVAR")
        .then()
        .statusCode(204);

    // Verify name stayed the same, quantity changed
    given()
        .when()
        .get(BASE_PATH + "/IVAR")
        .then()
        .statusCode(200)
        .body("name", equalTo("IVAR"))
        .body("quantityProductsInStock", equalTo(25));
  }

  @Test
  public void testPatchStore_UpdateNameOnly() {
    // First create a store
    String createRequest =
        """
        {
          "name": "LACK",
          "quantityProductsInStock": 40
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Patch only the name
    String patchRequest =
        """
        {
          "name": "LACK-UPDATED"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/LACK")
        .then()
        .statusCode(204);

    // Verify quantity stayed the same, name changed
    given()
        .when()
        .get(BASE_PATH + "/LACK-UPDATED")
        .then()
        .statusCode(200)
        .body("name", equalTo("LACK-UPDATED"))
        .body("quantityProductsInStock", equalTo(40));
  }

  @Test
  public void testPatchStore_UpdateBothFields() {
    // First create a store
    String createRequest =
        """
        {
          "name": "NORDLI",
          "quantityProductsInStock": 20
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Patch both fields
    String patchRequest =
        """
        {
          "name": "NORDLI-NEW",
          "quantityProductsInStock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/NORDLI")
        .then()
        .statusCode(204);

    // Verify both fields changed
    given()
        .when()
        .get(BASE_PATH + "/NORDLI-NEW")
        .then()
        .statusCode(200)
        .body("name", equalTo("NORDLI-NEW"))
        .body("quantityProductsInStock", equalTo(30));
  }

  @Test
  public void testPatchStore_EmptyPatch_KeepsExistingValues() {
    // First create a store
    String createRequest =
        """
        {
          "name": "EKTORP",
          "quantityProductsInStock": 12
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Patch with empty object (or only null/zero values)
    String patchRequest =
        """
        {
          "quantityProductsInStock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/EKTORP")
        .then()
        .statusCode(204);

    // Verify values stayed the same (zero is ignored in patch)
    given()
        .when()
        .get(BASE_PATH + "/EKTORP")
        .then()
        .statusCode(200)
        .body("name", equalTo("EKTORP"))
        .body("quantityProductsInStock", equalTo(12));
  }

  @Test
  public void testPatchStore_FailWhenNotFound() {
    String patchRequest =
        """
        {
          "quantityProductsInStock": 50
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/NONEXISTENT")
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteStore_Success() {
    // First create a store
    String createRequest =
        """
        {
          "name": "FJÄLLBO",
          "quantityProductsInStock": 8
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Delete it
    given()
        .when()
        .delete(BASE_PATH + "/FJÄLLBO")
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get(BASE_PATH + "/FJÄLLBO")
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteStore_FailWhenNotFound() {
    given()
        .when()
        .delete(BASE_PATH + "/NONEXISTENT")
        .then()
        .statusCode(404);
  }

  @Test
  public void testUpdateVsPatch_DifferentBehavior() {
    // Create a store
    String createRequest =
        """
        {
          "name": "COMPARISON",
          "quantityProductsInStock": 100
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Use PATCH to update only quantity - name should remain
    String patchRequest =
        """
        {
          "quantityProductsInStock": 200
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(patchRequest)
        .when()
        .patch(BASE_PATH + "/COMPARISON")
        .then()
        .statusCode(204);

    given()
        .when()
        .get(BASE_PATH + "/COMPARISON")
        .then()
        .statusCode(200)
        .body("name", equalTo("COMPARISON"))
        .body("quantityProductsInStock", equalTo(200));

    // Now use PUT (update) - should replace everything
    String updateRequest =
        """
        {
          "name": "COMPARISON",
          "quantityProductsInStock": 150
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/COMPARISON")
        .then()
        .statusCode(204);

    given()
        .when()
        .get(BASE_PATH + "/COMPARISON")
        .then()
        .statusCode(200)
        .body("name", equalTo("COMPARISON"))
        .body("quantityProductsInStock", equalTo(150));
  }
}