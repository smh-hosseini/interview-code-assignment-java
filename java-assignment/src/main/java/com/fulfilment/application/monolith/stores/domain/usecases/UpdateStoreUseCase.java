package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import com.fulfilment.application.monolith.stores.domain.ports.UpdateStoreOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;


@ApplicationScoped
public class UpdateStoreUseCase implements UpdateStoreOperation {

  private static final Logger LOG = Logger.getLogger(UpdateStoreUseCase.class);

  @Inject StoreStore storeStore;
  @Inject CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @Transactional
  @Override
  public void update(String storeName, Store updatedStore) {
    LOG.infof("Updating store: name=%s, newQuantity=%d", storeName, updatedStore.quantityProductsInStock());

    // Verify store exists using the path parameter
    storeStore.findByName(storeName).orElseThrow(
        () -> new StoreNotFoundException(storeName)
    );

    // Update the store with new values
    Store storeToUpdate = new Store(storeName, updatedStore.quantityProductsInStock());
    storeStore.update(storeToUpdate);
    LOG.infof("Store updated successfully: name=%s", storeName);

    // Create outbox event for legacy system integration
    createOutboxOutboxEventUseCase.create(new OutboxEvent(storeToUpdate, EventType.STORE_UPDATED, null));
  }

}
