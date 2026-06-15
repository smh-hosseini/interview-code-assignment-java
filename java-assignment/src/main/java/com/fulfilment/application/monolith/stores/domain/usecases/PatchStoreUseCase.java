package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.PatchStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class PatchStoreUseCase implements PatchStoreOperation {

  @Inject StoreStore storeStore;
  @Inject CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @Transactional
  @Override
  public void patch(String storeName, Store updatedStore) {
    // Verify store exists using the path parameter
    var existingStore = storeStore.findByName(storeName).orElseThrow(
        () -> new StoreNotFoundException(storeName)
    );

    // Merge values: use provided values if present, otherwise keep existing
    var name = updatedStore.name() != null ? updatedStore.name() : existingStore.name();
    var quantity = updatedStore.quantityProductsInStock() != null && updatedStore.quantityProductsInStock() != 0
        ? updatedStore.quantityProductsInStock()
        : existingStore.quantityProductsInStock();

    // Create patched store with merged values
    Store patchedStore = new Store(name, quantity);

    // Patch using original name to find the store, then apply patched values
    storeStore.patch(storeName, patchedStore);

    // Create outbox event for legacy system integration
    createOutboxOutboxEventUseCase.create(new OutboxEvent(patchedStore, EventType.STORE_UPDATED, null));
  }
}
