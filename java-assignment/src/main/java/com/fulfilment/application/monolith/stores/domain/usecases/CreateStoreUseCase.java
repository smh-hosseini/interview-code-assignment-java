package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.CreateStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import com.fulfilment.application.monolith.stores.domain.validator.StoreValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;


@ApplicationScoped
public class CreateStoreUseCase implements CreateStoreOperation {

  private static final Logger LOG = Logger.getLogger(CreateStoreUseCase.class);

  @Inject StoreStore storeStore;
  @Inject CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @Transactional
  @Override
  public void create(Store store) {
    LOG.infof("Creating store: name=%s, quantity=%d", store.name(), store.quantityProductsInStock());
    StoreValidator.validate(store);
    storeStore.create(store);
    LOG.infof("Store created successfully: name=%s", store.name());
    createOutboxOutboxEventUseCase.create(new OutboxEvent(store, EventType.STORE_CREATED, null));
  }
}
