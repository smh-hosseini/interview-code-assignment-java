package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import java.util.List;

public interface FulfillmentStore {

  void create(WarehouseFulfillment fulfillment);

  void remove(String warehouseCode, String productId, String storeId);

  List<WarehouseFulfillment> findByStore(String storeId);

  List<WarehouseFulfillment> findByWarehouse(String warehouseCode);

  List<WarehouseFulfillment> findByProductAndStore(String productId, String storeId);

  // Constraint checking queries
  long countWarehousesByProductAndStore(String productId, String storeId);

  long countWarehousesByStore(String storeId);

  long countProductsByWarehouse(String warehouseCode);

  boolean exists(String warehouseCode, String productId, String storeId);
}
