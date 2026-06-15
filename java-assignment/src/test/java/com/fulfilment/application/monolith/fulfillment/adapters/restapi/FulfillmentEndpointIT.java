package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentEndpointIT {

  private static final String BASE_PATH = "/fulfillment";

  @Test
  void createAndFindByProductAndStore_ShouldReturnAssociation() {
    createFulfillment("MWH.001", "92001", "91001");

    given()
        .queryParam("storeId", "91001")
        .queryParam("productId", "92001")
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].warehouseBusinessUnitCode", equalTo("MWH.001"))
        .body("[0].storeId", equalTo("91001"))
        .body("[0].productId", equalTo("92001"))
        .body("[0].priority", greaterThanOrEqualTo(1));
  }

  @Test
  void findByStoreAndWarehouse_ShouldReturnExpectedResults() {
    createFulfillment("MWH.001", "92002", "91002");
    createFulfillment("MWH.012", "92002", "91002");

    given()
        .queryParam("storeId", "91002")
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("size()", equalTo(2));

    given()
        .queryParam("warehouseCode", "MWH.012")
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("findAll { it.storeId == '91002' && it.productId == '92002' }.size()", equalTo(1));
  }

  @Test
  void getWithUnknownStoreAndProduct_ShouldReturnEmptyList() {
    given()
        .queryParam("storeId", "999999")
        .queryParam("productId", "999999")
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("$", empty());
  }

  @Test
  void removeExistingAssociation_ShouldReturnNoContent() {
    createFulfillment("MWH.023", "92003", "91003");

    given()
        .when()
        .delete(BASE_PATH + "/MWH.023/92003/91003")
        .then()
        .statusCode(204);

    given()
        .queryParam("storeId", "91003")
        .queryParam("productId", "92003")
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("$", empty());
  }

  @Test
  void createWithUnknownWarehouse_ShouldReturnValidationError() {
    String requestBody =
        """
        {
          "warehouseBusinessUnitCode": "MWH.999",
          "productId": "92004",
          "storeId": "91004"
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("errorCode", equalTo("FULFILLMENT_VALIDATION_ERROR"))
        .body("message", containsString("Warehouse 'MWH.999' not found"));
  }

  @Test
  void removeMissingAssociation_ShouldReturnNotFound() {
    given()
        .when()
        .delete(BASE_PATH + "/MWH.001/99999/99999")
        .then()
        .statusCode(404)
        .body("errorCode", equalTo("FULFILLMENT_NOT_FOUND"));
  }

  private static void createFulfillment(String warehouseCode, String productId, String storeId) {
    String requestBody =
        """
        {
          "warehouseBusinessUnitCode": "%s",
          "productId": "%s",
          "storeId": "%s"
        }
        """
            .formatted(warehouseCode, productId, storeId);

    given().contentType(ContentType.JSON).body(requestBody).when().post(BASE_PATH).then().statusCode(201);
  }
}
