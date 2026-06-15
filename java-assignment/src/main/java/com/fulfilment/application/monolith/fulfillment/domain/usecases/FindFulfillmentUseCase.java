package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FindFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FindFulfillmentUseCase implements FindFulfillmentOperation {

  private final FulfillmentStore fulfillmentStore;

  public FindFulfillmentUseCase(FulfillmentStore fulfillmentStore) {
    this.fulfillmentStore = fulfillmentStore;
  }

  @Override
  public List<WarehouseFulfillment> findByStore(String storeId) {
    return fulfillmentStore.findByStore(storeId);
  }

  @Override
  public List<WarehouseFulfillment> findByWarehouse(String warehouseCode) {
    return fulfillmentStore.findByWarehouse(warehouseCode);
  }

  @Override
  public List<WarehouseFulfillment> findByProductAndStore(String productId, String storeId) {
    return fulfillmentStore.findByProductAndStore(productId, storeId);
  }
}
