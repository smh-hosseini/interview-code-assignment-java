package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

public interface LegacyStoreGateway {

  void createStoreOnLegacySystem(Store store);
  void updateStoreOnLegacySystem(Store store);
}
