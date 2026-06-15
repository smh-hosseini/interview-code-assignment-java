package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentValidationException;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.CreateFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateFulfillmentUseCase implements CreateFulfillmentOperation {

  private final FulfillmentStore fulfillmentStore;
  private final WarehouseStore warehouseStore;

  public CreateFulfillmentUseCase(
      FulfillmentStore fulfillmentStore,
      WarehouseStore warehouseStore) {
    this.fulfillmentStore = fulfillmentStore;
    this.warehouseStore = warehouseStore;
  }

  @Transactional
  @Override
  public void create(WarehouseFulfillment fulfillment) {
    // 1. Validate warehouse exists and is active
    Warehouse warehouse =
        warehouseStore
            .findByBusinessUnitCode(fulfillment.warehouseBusinessUnitCode())
            .orElseThrow(
                () ->
                    new FulfillmentValidationException(
                        "Warehouse '" + fulfillment.warehouseBusinessUnitCode() + "' not found"));

    if (warehouse.archivedAt() != null) {
      throw new FulfillmentValidationException(
          "Cannot associate archived warehouse '" + fulfillment.warehouseBusinessUnitCode() + "'");
    }

    // 2. CONSTRAINT 1: Each Product can be fulfilled by max 2 Warehouses per Store
    long currentWarehouses =
        fulfillmentStore.countWarehousesByProductAndStore(
            fulfillment.productId(), fulfillment.storeId());
    if (currentWarehouses >= 2) {
      throw new FulfillmentValidationException(
          "Product '"
              + fulfillment.productId()
              + "' already has maximum (2) warehouses for store '"
              + fulfillment.storeId()
              + "'");
    }

    // Set priority based on existing associations (1 = primary, 2 = secondary)
    int priority = (int) currentWarehouses + 1;

    // 3. CONSTRAINT 2: Each Store can be fulfilled by max 3 Warehouses
    long warehousesForStore = fulfillmentStore.countWarehousesByStore(fulfillment.storeId());
    if (warehousesForStore >= 3) {
      throw new FulfillmentValidationException(
          "Store '" + fulfillment.storeId() + "' already has maximum (3) fulfillment warehouses");
    }

    // 4. CONSTRAINT 3: Each Warehouse can store max 5 types of Products
    long productsInWarehouse =
        fulfillmentStore.countProductsByWarehouse(fulfillment.warehouseBusinessUnitCode());
    if (productsInWarehouse >= 5) {
      throw new FulfillmentValidationException(
          "Warehouse '"
              + fulfillment.warehouseBusinessUnitCode()
              + "' already stores maximum (5) product types");
    }

    // 5. Create the association with priority and timestamp
    WarehouseFulfillment fulfillmentWithPriority =
        new WarehouseFulfillment(
            fulfillment.warehouseBusinessUnitCode(),
            fulfillment.productId(),
            fulfillment.storeId(),
            priority,
            null); // createdAt set by @CreationTimestamp

    fulfillmentStore.create(fulfillmentWithPriority);
  }
}
