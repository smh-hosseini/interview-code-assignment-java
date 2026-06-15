package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseStore {

  List<Warehouse> getAll();

  /**
   * Creates a new warehouse.
   */
  void create(Warehouse warehouse);

  /**
   * Updates an existing warehouse.
   */
  void update(Warehouse warehouse);

  /**
   * Archive a warehouse.
   */
  void archive(Warehouse warehouse);

  /**
   * Finds a warehouse by business unit code. Returns optional.
   * Only returns active (non-archived) warehouses.
   */
  Optional<Warehouse> findByBusinessUnitCode(String buCode);

}
