package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointIT {

  private static final String BASE_PATH = "/warehouse";

  @Test
  public void testListAllWarehouses() {
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("businessUnitCode", hasItems("MWH.001", "MWH.012", "MWH.023"));
  }

  @Test
  public void testGetWarehouseById_Success() {
    given()
        .when()
        .get(BASE_PATH + "/MWH.001")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.001"))
        .body("location", equalTo("ZWOLLE-001"));
  }

  @Test
  public void testGetWarehouseById_FailWhenNotFound() {
    given()
        .when()
        .get(BASE_PATH + "/MWH.999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testCreateWarehouse_Success() {
    // Use ZWOLLE-002 which has maxCapacity: 50 and no existing warehouses
    String requestBody =
        """
        {
          "businessUnitCode": "MWH.100",
          "location": "ZWOLLE-002",
          "capacity": 40,
          "stock": 20
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
        .get(BASE_PATH + "/MWH.100")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.100"))
        .body("location", equalTo("ZWOLLE-002"))
        .body("capacity", equalTo(40))
        .body("stock", equalTo(20));
  }

  @Test
  public void testCreateWarehouse_FailWhenDuplicateBusinessUnitCode() {
    String requestBody =
        """
        {
          "businessUnitCode": "MWH.001",
          "location": "AMSTERDAM-001",
          "capacity": 50,
          "stock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }

  @Test
  public void testCreateWarehouse_FailWhenLocationDoesNotExist() {
    String requestBody =
        """
        {
          "businessUnitCode": "MWH.101",
          "location": "INVALID-LOCATION",
          "capacity": 50,
          "stock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  public void testCreateWarehouse_FailWhenCapacityLessThanStock() {
    String requestBody =
        """
        {
          "businessUnitCode": "MWH.102",
          "location": "AMSTERDAM-001",
          "capacity": 30,
          "stock": 50
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(400)
        .body("message", containsString("capacity"));
  }

  @Test
  public void testArchiveWarehouse_Success() {
    // First create a warehouse to archive (AMSTERDAM-001 has 50 capacity available)
    String createRequest =
        """
        {
          "businessUnitCode": "MWH.200",
          "location": "AMSTERDAM-001",
          "capacity": 40,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Archive it
    given()
        .when()
        .delete(BASE_PATH + "/MWH.200")
        .then()
        .statusCode(204);

    // Verify it's archived (archived warehouses are not accessible via GET)
    given()
        .when()
        .get(BASE_PATH + "/MWH.200")
        .then()
        .statusCode(404);
  }

  @Test
  public void testArchiveWarehouse_FailWhenNotFound() {
    given()
        .when()
        .delete(BASE_PATH + "/MWH.999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testArchiveWarehouse_FailWhenAlreadyArchived() {
    // Create and archive a warehouse (use EINDHOVEN-001 which has capacity available)
    String createRequest =
        """
        {
          "businessUnitCode": "MWH.201",
          "location": "EINDHOVEN-001",
          "capacity": 50,
          "stock": 0
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Archive it
    given()
        .delete(BASE_PATH + "/MWH.201")
        .then()
        .statusCode(204);

    // Try to archive again - should fail because warehouse is already archived
    given()
        .when()
        .delete(BASE_PATH + "/MWH.201")
        .then()
        .statusCode(404)
        .body("message", containsString("not found"));
  }

  @Test
  public void testReplaceWarehouse_Success() {
    // Create a warehouse to replace (use HELMOND-001 which has capacity available)
    String createRequest =
        """
        {
          "businessUnitCode": "MWH.300",
          "location": "HELMOND-001",
          "capacity": 35,
          "stock": 20
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Replace it with a new one (same stock, different location and capacity)
    String replaceRequest =
        """
        {
          "location": "AMSTERDAM-002",
          "capacity": 50,
          "stock": 20
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(replaceRequest)
        .when()
        .post(BASE_PATH + "/MWH.300/replacement")
        .then()
        .statusCode(201);

    // Verify old warehouse is archived and new one exists
    given()
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("businessUnitCode", hasItem("MWH.300"));
  }

  @Test
  public void testReplaceWarehouse_FailWhenWarehouseNotFound() {
    String replaceRequest =
        """
        {
          "location": "AMSTERDAM-001",
          "capacity": 70,
          "stock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(replaceRequest)
        .when()
        .post(BASE_PATH + "/MWH.999/replacement")
        .then()
        .statusCode(404);
  }

  @Test
  public void testReplaceWarehouse_FailWhenStockDoesNotMatch() {
    // Create a warehouse (use VETSBY-001 which has capacity available)
    String createRequest =
        """
        {
          "businessUnitCode": "MWH.301",
          "location": "VETSBY-001",
          "capacity": 60,
          "stock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post(BASE_PATH);

    // Try to replace with different stock
    String replaceRequest =
        """
        {
          "location": "ZWOLLE-002",
          "capacity": 40,
          "stock": 35
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(replaceRequest)
        .when()
        .post(BASE_PATH + "/MWH.301/replacement")
        .then()
        .statusCode(400)
        .body("message", containsStringIgnoringCase("stock must match"));
  }

  @Test
  public void testReplaceWarehouse_FailWhenNewCapacityLessThanStock() {
    // Create a warehouse (use HELMOND-001 which has capacity available)
    String createRequest =
        """
        {
          "businessUnitCode": "MWH.302",
          "location": "HELMOND-001",
          "capacity": 40,
          "stock": 35
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Try to replace with capacity less than stock
    String replaceRequest =
        """
        {
          "location": "EINDHOVEN-001",
          "capacity": 30,
          "stock": 35
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(replaceRequest)
        .when()
        .post(BASE_PATH + "/MWH.302/replacement")
        .then()
        .statusCode(400)
        .body("message", containsString("cannot accommodate"));
  }
}
