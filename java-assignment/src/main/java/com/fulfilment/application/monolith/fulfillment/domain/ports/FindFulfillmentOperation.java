package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import java.util.List;

public interface FindFulfillmentOperation {
  List<WarehouseFulfillment> findByStore(String storeId);

  List<WarehouseFulfillment> findByWarehouse(String warehouseCode);

  List<WarehouseFulfillment> findByProductAndStore(String productId, String storeId);
}
