package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator validator;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }

  @Transactional
  @Override
  public void archive(Warehouse warehouse) {
    // 1-2. Validate input
    validator.validateWarehouseNotNull(warehouse);
    validator.validateBusinessUnitCode(warehouse.businessUnitCode());

    // 3. Check if warehouse exists (only returns active warehouses)
    warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode())
        .orElseThrow(() -> new WarehouseNotFoundException(warehouse.businessUnitCode()));

    // 4. Archive the warehouse
    warehouseStore.archive(warehouse);
  }

  public void archiveByBusinessUnitCode(String businessUnitCode) {
    validator.validateBusinessUnitCode(businessUnitCode);
    Warehouse warehouse = new Warehouse(businessUnitCode, null, null, null, null, null);
    archive(warehouse);
  }
}
