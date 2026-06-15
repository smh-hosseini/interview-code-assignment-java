package com.fulfilment.application.monolith.fulfillment.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentAlreadyExistsException;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentRepositoryIT {

  @Inject FulfillmentRepository fulfillmentRepository;

  @Test
  @TestTransaction
  void createShouldPersistFulfillmentAndMarkItAsExisting() {
    var fulfillment = new WarehouseFulfillment("REP.F01", "701", "801", 1, null);

    fulfillmentRepository.create(fulfillment);

    assertTrue(fulfillmentRepository.exists("REP.F01", "701", "801"));
  }

  @Test
  @TestTransaction
  void createShouldFailWhenFulfillmentAlreadyExists() {
    var fulfillment = new WarehouseFulfillment("REP.F02", "702", "802", 1, null);
    fulfillmentRepository.create(fulfillment);

    assertThrows(
        FulfillmentAlreadyExistsException.class,
        () -> fulfillmentRepository.create(fulfillment));
  }

  @Test
  @TestTransaction
  void removeShouldDeleteFulfillment() {
    var fulfillment = new WarehouseFulfillment("REP.F03", "703", "803", 1, null);
    fulfillmentRepository.create(fulfillment);
    assertTrue(fulfillmentRepository.exists("REP.F03", "703", "803"));

    fulfillmentRepository.remove("REP.F03", "703", "803");

    assertFalse(fulfillmentRepository.exists("REP.F03", "703", "803"));
  }

  @Test
  @TestTransaction
  void findByStoreShouldReturnStoreFulfillments() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F04", "704", "804", 1, null));

    var fulfillments = fulfillmentRepository.findByStore("804");

    assertFalse(fulfillments.isEmpty());
    assertTrue(
        fulfillments.stream()
            .anyMatch(f -> "REP.F04".equals(f.warehouseBusinessUnitCode()) && "704".equals(f.productId())));
  }

  @Test
  @TestTransaction
  void findByWarehouseShouldReturnWarehouseFulfillments() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F05", "705", "805", 1, null));

    var fulfillments = fulfillmentRepository.findByWarehouse("REP.F05");

    assertFalse(fulfillments.isEmpty());
    assertTrue(
        fulfillments.stream()
            .anyMatch(f -> "705".equals(f.productId()) && "805".equals(f.storeId())));
  }

  @Test
  @TestTransaction
  void findByProductAndStoreShouldReturnMatchingFulfillments() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F06", "706", "806", 1, null));

    var fulfillments = fulfillmentRepository.findByProductAndStore("706", "806");

    assertEquals(1, fulfillments.size());
    assertEquals("REP.F06", fulfillments.get(0).warehouseBusinessUnitCode());
  }

  @Test
  @TestTransaction
  void countWarehousesByProductAndStoreShouldReturnCount() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F07", "707", "807", 1, null));

    long count = fulfillmentRepository.countWarehousesByProductAndStore("707", "807");

    assertEquals(1L, count);
  }

  @Test
  @TestTransaction
  void countWarehousesByStoreShouldReturnCountForStore() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F08", "708", "808", 1, null));

    long count = fulfillmentRepository.countWarehousesByStore("808");

    assertEquals(1L, count);
  }

  @Test
  @TestTransaction
  void countProductsByWarehouseShouldReturnCountForWarehouse() {
    fulfillmentRepository.create(new WarehouseFulfillment("REP.F09", "709", "809", 1, null));

    long count = fulfillmentRepository.countProductsByWarehouse("REP.F09");

    assertEquals(1L, count);
  }
}
