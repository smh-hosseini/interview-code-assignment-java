package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.domain.ports.RemoveFulfillmentOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RemoveFulfillmentUseCase implements RemoveFulfillmentOperation {

  private final FulfillmentStore fulfillmentStore;

  public RemoveFulfillmentUseCase(FulfillmentStore fulfillmentStore) {
    this.fulfillmentStore = fulfillmentStore;
  }

  @Transactional
  @Override
  public void remove(String warehouseCode, String productId, String storeId) {
    // Validate association exists
    if (!fulfillmentStore.exists(warehouseCode, productId, storeId)) {
      throw new FulfillmentNotFoundException(warehouseCode, productId, storeId);
    }

    fulfillmentStore.remove(warehouseCode, productId, storeId);
  }
}
