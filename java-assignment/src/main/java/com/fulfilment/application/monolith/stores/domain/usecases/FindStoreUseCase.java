package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.FindStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class FindStoreUseCase implements FindStoreOperation {

  @Inject
  StoreStore storeStore;

  @Override
  public List<Store> findAll() {
    return storeStore.findAllStores();
  }

  @Override
  public Store findByName(String storeName) {
    return storeStore.findByName(storeName).orElseThrow(() -> new StoreNotFoundException(storeName));
  }
}
