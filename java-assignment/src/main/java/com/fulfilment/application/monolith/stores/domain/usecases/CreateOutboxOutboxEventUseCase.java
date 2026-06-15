package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.ports.CreateOutboxEventOperation;
import com.fulfilment.application.monolith.stores.domain.ports.OutboxEventStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateOutboxOutboxEventUseCase implements CreateOutboxEventOperation {

  private static final Logger LOG = Logger.getLogger(CreateOutboxOutboxEventUseCase.class);

  @Inject OutboxEventStore store;

  @Transactional
  @Override
  public void create(OutboxEvent event) {
    LOG.infof("Creating outbox event: type=%s, storeName=%s", event.eventType(), event.store().name());
    store.create(event);
    LOG.infof("Outbox event created successfully: type=%s, storeName=%s", event.eventType(), event.store().name());
  }

}
