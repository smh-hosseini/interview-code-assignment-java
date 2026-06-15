package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryIT {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  @TestTransaction
  void createShouldPersistWarehouse() {
    Warehouse warehouse = new Warehouse("REP.901", "COV-LOC-001", 20, 5, null, null);
    warehouseRepository.create(warehouse);

    Optional<Warehouse> found = warehouseRepository.findByBusinessUnitCode("REP.901");
    assertTrue(found.isPresent());
    assertEquals("COV-LOC-001", found.get().location());
    assertEquals(20, found.get().capacity());
    assertEquals(5, found.get().stock());
  }

  @Test
  @TestTransaction
  void createShouldFailWhenActiveWarehouseWithSameBusinessUnitExists() {
    warehouseRepository.create(new Warehouse("REP.902", "COV-LOC-002", 10, 1, null, null));

    assertThrows(
        WarehouseAlreadyExistsException.class,
        () -> warehouseRepository.create(new Warehouse("REP.902", "COV-LOC-003", 15, 2, null, null)));
  }

  @Test
  @TestTransaction
  void createShouldAllowNewActiveWarehouseWhenPreviousIsArchived() {
    warehouseRepository.create(new Warehouse("REP.903", "COV-LOC-010", 10, 1, null, null));
    warehouseRepository.archive(new Warehouse("REP.903", null, null, null, null, null));

    warehouseRepository.create(new Warehouse("REP.903", "COV-LOC-011", 30, 2, null, null));

    Optional<Warehouse> active = warehouseRepository.findByBusinessUnitCode("REP.903");
    assertTrue(active.isPresent());
    assertEquals("COV-LOC-011", active.get().location());
    assertNull(active.get().archivedAt());
  }

  @Test
  @TestTransaction
  void updateShouldPersistChanges() {
    warehouseRepository.create(new Warehouse("REP.904", "COV-LOC-020", 30, 10, null, null));
    warehouseRepository.update(new Warehouse("REP.904", "COV-LOC-021", 35, 11, null, null));

    Optional<Warehouse> updated = warehouseRepository.findByBusinessUnitCode("REP.904");
    assertTrue(updated.isPresent());
    assertEquals("COV-LOC-021", updated.get().location());
    assertEquals(35, updated.get().capacity());
    assertEquals(11, updated.get().stock());
  }

  @Test
  @TestTransaction
  void updateShouldFailWhenWarehouseIsMissing() {
    assertThrows(
        WarehouseNotFoundException.class,
        () -> warehouseRepository.update(new Warehouse("REP.905", "COV-LOC-030", 10, 1, null, null)));
  }

  @Test
  @TestTransaction
  void archiveShouldMarkWarehouseAsArchivedAndHideItFromActiveLookup() {
    warehouseRepository.create(new Warehouse("REP.906", "COV-LOC-040", 12, 3, null, null));
    warehouseRepository.archive(new Warehouse("REP.906", null, null, null, null, null));

    assertTrue(warehouseRepository.findByBusinessUnitCode("REP.906").isEmpty());
  }

  @Test
  @TestTransaction
  void archiveShouldFailWhenWarehouseIsMissing() {
    assertThrows(
        WarehouseNotFoundException.class,
        () -> warehouseRepository.archive(new Warehouse("REP.907", null, null, null, null, null)));
  }

  @Test
  @TestTransaction
  void countAndSumByLocationShouldConsiderOnlyActiveWarehouses() {
    warehouseRepository.create(new Warehouse("REP.910", "COV-LOC-050", 10, 1, null, null));
    warehouseRepository.create(new Warehouse("REP.911", "COV-LOC-050", 15, 2, null, null));
    warehouseRepository.archive(new Warehouse("REP.911", null, null, null, null, null));

    long activeCount = warehouseRepository.countActiveWarehousesByLocation("COV-LOC-050");
    int activeCapacity = warehouseRepository.sumActiveCapacityByLocation("COV-LOC-050");

    assertEquals(1L, activeCount);
    assertEquals(10, activeCapacity);
  }

  @Test
  @TestTransaction
  void getAllShouldReturnWarehousesIncludingSeedData() {
    var all = warehouseRepository.getAll();
    assertFalse(all.isEmpty());
    assertTrue(all.stream().anyMatch(warehouse -> "MWH.001".equals(warehouse.businessUnitCode())));
  }
}
