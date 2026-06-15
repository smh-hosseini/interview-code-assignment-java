package com.fulfilment.application.monolith.warehouses.domain.validation;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.ObjectUtils;

@ApplicationScoped
public class WarehouseValidator {

  private static final String BUSINESS_UNIT_CODE_PATTERN = "^[A-Z]{3}\\.[0-9]{3}$";

  public void validateWarehouse(Warehouse warehouse) {
    validateWarehouseNotNull(warehouse);
    validateBusinessUnitCode(warehouse.businessUnitCode());
    validateLocation(warehouse.location());
    validateCapacity(warehouse.capacity());
    validateStock(warehouse.stock());
    validateCapacityCanHoldStock(warehouse.capacity(), warehouse.stock());
  }

  public void validateWarehouseNotNull(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse data is required");
    }
  }

  public void validateBusinessUnitCode(String businessUnitCode) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Business unit code is required");
    }

    if (!businessUnitCode.matches(BUSINESS_UNIT_CODE_PATTERN)) {
      throw new WarehouseValidationException(
          "Business unit code must match format XXX.NNN (e.g., MWH.001)");
    }
  }

  public void validateLocation(String location) {
    if (ObjectUtils.isEmpty(location)) {
      throw new WarehouseValidationException("Location is required");
    }
  }

  public void validateCapacity(Integer capacity) {
    if (capacity == null) {
      throw new WarehouseValidationException("Capacity is required");
    }

    if (capacity < 0) {
      throw new WarehouseValidationException("Capacity cannot be negative");
    }
  }

  public void validateStock(Integer stock) {
    if (stock == null) {
      throw new WarehouseValidationException("Stock is required");
    }

    if (stock < 0) {
      throw new WarehouseValidationException("Stock cannot be negative");
    }
  }

  public void validateCapacityCanHoldStock(Integer capacity, Integer stock) {
    if (capacity < stock) {
      throw new WarehouseValidationException(
          "Warehouse capacity (" + capacity + ") cannot be less than stock (" + stock + ")");
    }
  }
}