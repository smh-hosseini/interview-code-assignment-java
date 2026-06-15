package com.fulfilment.application.monolith.fulfillment.domain.ports;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;

public interface CreateFulfillmentOperation {
  void create(WarehouseFulfillment fulfillment);
}
