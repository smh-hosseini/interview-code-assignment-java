package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final CreateWarehouseOperation createWarehouseOperation;
  private final WarehouseValidator validator;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore,
      CreateWarehouseOperation createWarehouseOperation,
      WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.createWarehouseOperation = createWarehouseOperation;
    this.validator = validator;
  }

  @Transactional
  @Override
  public void replace(Warehouse newWarehouse) {
    // 1-5. Validate basic warehouse data
    validator.validateWarehouseNotNull(newWarehouse);
    validator.validateBusinessUnitCode(newWarehouse.businessUnitCode());
    validator.validateLocation(newWarehouse.location());
    validator.validateCapacity(newWarehouse.capacity());
    validator.validateStock(newWarehouse.stock());

    // 6. Get existing warehouse (throws WarehouseNotFoundException if not found)
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode())
        .orElseThrow(() -> new WarehouseNotFoundException(newWarehouse.businessUnitCode()));

    // 8. Validate new capacity can accommodate existing stock
    if (newWarehouse.capacity() < existing.stock()) {
      throw new WarehouseValidationException(
          "New capacity ("
              + newWarehouse.capacity()
              + ") cannot accommodate existing stock ("
              + existing.stock()
              + ")");
    }

    // 9. Validate stock matches previous warehouse
    if (!newWarehouse.stock().equals(existing.stock())) {
      throw new WarehouseValidationException(
          "Stock must match previous warehouse stock (" + existing.stock() + ")");
    }

    // 10. Archive (supersede) the old warehouse
    // Create a new warehouse instance with archivedAt timestamp
    Warehouse archivedWarehouse = new Warehouse(
        existing.businessUnitCode(),
        existing.location(),
        existing.capacity(),
        existing.stock(),
        existing.createdAt(),
        LocalDateTime.now()
    );
    warehouseStore.update(archivedWarehouse);

    // 11. Create the new warehouse version with the same businessUnitCode
    // The create operation will validate location constraints, capacity, etc.
    createWarehouseOperation.create(newWarehouse);
  }
}
