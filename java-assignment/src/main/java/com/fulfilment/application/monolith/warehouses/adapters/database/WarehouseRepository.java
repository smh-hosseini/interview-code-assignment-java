package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {

    // Check constraint: only one active warehouse per businessUnitCode (partial unique index)
    if (findCurrentByCode(warehouse.businessUnitCode()).isPresent()) {
      throw new WarehouseAlreadyExistsException(warehouse.businessUnitCode());
    }

    try {
      DbWarehouse dbWarehouse = mapToDbWarehouse(warehouse);
      // Version is managed by JPA @Version annotation for optimistic locking
      persist(dbWarehouse);
      flush(); // Force constraint validation
    } catch (PersistenceException e) {
      // Safety net for race conditions (database partial index violation)
      if (e.getCause() instanceof ConstraintViolationException) {
        throw new WarehouseAlreadyExistsException(warehouse.businessUnitCode());
      }
      throw e; // Re-throw if it's not a duplicate key error
    }
  }

  @Override
  public void update(Warehouse warehouse) {
    // Find any warehouse (active or archived) to allow updating archive status
    DbWarehouse dbWarehouse = findDbWarehouseByCode(warehouse.businessUnitCode())
        .orElseThrow(() -> new WarehouseNotFoundException(warehouse.businessUnitCode()));

    dbWarehouse.location = warehouse.location();
    dbWarehouse.capacity = warehouse.capacity();
    dbWarehouse.stock = warehouse.stock();
    dbWarehouse.archivedAt = warehouse.archivedAt();
    persist(dbWarehouse);
  }

  @Override
  public void archive(Warehouse warehouse) {
    var dbWarehouse = findCurrentByCode(warehouse.businessUnitCode())
        .orElseThrow(() -> new WarehouseNotFoundException(warehouse.businessUnitCode()));

    dbWarehouse.archivedAt = LocalDateTime.now();
  }

  @Override
  public Optional<Warehouse> findByBusinessUnitCode(String buCode) {
    // Find current (active) warehouse by businessUnitCode
    return findCurrentByCode(buCode).map(DbWarehouse::toWarehouse);
  }

  // Find current (active) version only
  private Optional<DbWarehouse> findCurrentByCode(String buCode) {
    return find("businessUnitCode = ?1 and archivedAt IS NULL", buCode).firstResultOptional();
  }

  // Find any version (for internal use)
  private Optional<DbWarehouse> findDbWarehouseByCode(String buCode) {
    return find("businessUnitCode", buCode).firstResultOptional();
  }

  // Helper methods for warehouse validations
  public long countActiveWarehousesByLocation(String location) {
    return count("location = ?1 and archivedAt IS NULL", location);
  }

  public int sumActiveCapacityByLocation(String location) {
    return find("location = ?1 and archivedAt IS NULL", location).stream()
        .mapToInt(wh -> wh.capacity != null ? wh.capacity : 0)
        .sum();
  }

  private static DbWarehouse mapToDbWarehouse(Warehouse warehouse) {
      DbWarehouse dbWarehouse = new DbWarehouse();
      dbWarehouse.businessUnitCode = warehouse.businessUnitCode();
      dbWarehouse.location = warehouse.location();
      dbWarehouse.capacity = warehouse.capacity();
      dbWarehouse.stock = warehouse.stock();
      dbWarehouse.createdAt = warehouse.createdAt();
      return dbWarehouse;
  }
}
