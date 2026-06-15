package com.fulfilment.application.monolith.fulfillment.domain.ports;

public interface RemoveFulfillmentOperation {
  void remove(String warehouseCode, String productId, String storeId);
}
