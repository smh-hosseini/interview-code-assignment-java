package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentAlreadyExistsException;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

@ApplicationScoped
public class FulfillmentRepository
    implements FulfillmentStore, PanacheRepository<DbWarehouseFulfillment> {

  @Override
  public void create(WarehouseFulfillment fulfillment) {
    // Check if already exists
    if (exists(
        fulfillment.warehouseBusinessUnitCode(),
        fulfillment.productId(),
        fulfillment.storeId())) {
      throw new FulfillmentAlreadyExistsException(
          fulfillment.warehouseBusinessUnitCode(), fulfillment.productId(), fulfillment.storeId());
    }

    try {
      DbWarehouseFulfillment dbFulfillment = mapToDb(fulfillment);
      persist(dbFulfillment);
      flush();
    } catch (PersistenceException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        throw new FulfillmentAlreadyExistsException(
            fulfillment.warehouseBusinessUnitCode(), fulfillment.productId(), fulfillment.storeId());
      }
      throw e;
    }
  }

  @Override
  public void remove(String warehouseCode, String productId, String storeId) {
    delete(
        "warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
        warehouseCode,
        Long.parseLong(productId),
        Long.parseLong(storeId));
  }

  @Override
  public List<WarehouseFulfillment> findByStore(String storeId) {
    return find("storeId", Long.parseLong(storeId)).stream()
        .map(DbWarehouseFulfillment::toWarehouseFulfillment)
        .toList();
  }

  @Override
  public List<WarehouseFulfillment> findByWarehouse(String warehouseCode) {
    return find("warehouseBusinessUnitCode", warehouseCode).stream()
        .map(DbWarehouseFulfillment::toWarehouseFulfillment)
        .toList();
  }

  @Override
  public List<WarehouseFulfillment> findByProductAndStore(String productId, String storeId) {
    return find("productId = ?1 and storeId = ?2", Long.parseLong(productId), Long.parseLong(storeId))
        .stream()
        .map(DbWarehouseFulfillment::toWarehouseFulfillment)
        .toList();
  }

  @Override
  public long countWarehousesByProductAndStore(String productId, String storeId) {
    return count("productId = ?1 and storeId = ?2", Long.parseLong(productId), Long.parseLong(storeId));
  }

  @Override
  public long countWarehousesByStore(String storeId) {
    return find(
            "select count(distinct warehouseBusinessUnitCode) from DbWarehouseFulfillment where storeId = ?1",
            Long.parseLong(storeId))
        .count();
  }

  @Override
  public long countProductsByWarehouse(String warehouseCode) {
    return find(
            "select count(distinct productId) from DbWarehouseFulfillment where warehouseBusinessUnitCode = ?1",
            warehouseCode)
        .count();
  }

  @Override
  public boolean exists(String warehouseCode, String productId, String storeId) {
    return count(
            "warehouseBusinessUnitCode = ?1 and productId = ?2 and storeId = ?3",
            warehouseCode,
            Long.parseLong(productId),
            Long.parseLong(storeId))
        > 0;
  }

  private DbWarehouseFulfillment mapToDb(WarehouseFulfillment fulfillment) {
    DbWarehouseFulfillment db = new DbWarehouseFulfillment();
    db.warehouseBusinessUnitCode = fulfillment.warehouseBusinessUnitCode();
    db.productId = Long.parseLong(fulfillment.productId());
    db.storeId = Long.parseLong(fulfillment.storeId());
    db.priority = fulfillment.priority();
    return db;
  }
}
