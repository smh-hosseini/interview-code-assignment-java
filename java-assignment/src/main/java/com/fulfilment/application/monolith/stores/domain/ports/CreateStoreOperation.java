package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

public interface CreateStoreOperation {

  void create(Store store);
}
