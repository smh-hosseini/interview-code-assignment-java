package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.ports.RemoveStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DeleteStoreUseCase implements RemoveStoreOperation {

  @Inject
  StoreStore storeStore;

  @Transactional
  @Override
  public void remove(String storeName) {
    storeStore.findByName(storeName).orElseThrow(() -> new StoreNotFoundException(storeName));
    storeStore.delete(storeName);
  }
}
