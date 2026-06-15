package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseValidator validator;

  public CreateWarehouseUseCase(
      WarehouseStore warehouseStore,
      LocationResolver locationResolver,
      WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator;
  }

  @Transactional
  @Override
  public void create(Warehouse warehouse) {
    // 1-5. Validate basic warehouse data
    validator.validateWarehouseNotNull(warehouse);
    validator.validateBusinessUnitCode(warehouse.businessUnitCode());
    validator.validateLocation(warehouse.location());
    validator.validateCapacity(warehouse.capacity());
    validator.validateStock(warehouse.stock());

    // 6. Check if active warehouse with same business unit code already exists
    warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode())
        .filter(existing -> existing.archivedAt() == null)
        .ifPresent(existing -> {
          throw new WarehouseAlreadyExistsException(warehouse.businessUnitCode());
        });

    // 7. Validate location exists
    Location location = locationResolver.resolveByIdentifier(warehouse.location());
    if (location == null) {
      throw new WarehouseValidationException(
          "Location '" + warehouse.location() + "' does not exist");
    }

    // 8. Validate max number of warehouses at location
    long currentCount =
        ((WarehouseRepository) warehouseStore)
            .countActiveWarehousesByLocation(warehouse.location());
    if (currentCount >= location.maxNumberOfWarehouses()) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses()
              + ") reached at location '"
              + warehouse.location()
              + "'");
    }

    // 9. Validate total capacity doesn't exceed location max
    int currentCapacity =
        ((WarehouseRepository) warehouseStore)
            .sumActiveCapacityByLocation(warehouse.location());
    if (currentCapacity + warehouse.capacity() > location.maxCapacity()) {
      throw new WarehouseValidationException(
          "Total capacity would exceed location maximum of " + location.maxCapacity());
    }

    // 10. Validate capacity can handle stock
    validator.validateCapacityCanHoldStock(warehouse.capacity(), warehouse.stock());

    // 12. Create the warehouse
    warehouseStore.create(warehouse);
  }
}
