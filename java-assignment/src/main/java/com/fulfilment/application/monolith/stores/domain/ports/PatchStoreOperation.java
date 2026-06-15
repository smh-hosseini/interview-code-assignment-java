package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

public interface PatchStoreOperation {

  void patch(String storeName, Store store);
}
