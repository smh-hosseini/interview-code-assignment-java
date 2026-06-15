package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.ports.LegacyStoreGateway;
import com.fulfilment.application.monolith.stores.domain.ports.NotifyLegacyStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.OutboxEventStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NotifyLegacyStoreUseCase implements NotifyLegacyStoreOperation {

  private static final Logger LOGGER = Logger.getLogger(NotifyLegacyStoreUseCase.class.getName());

  @Inject OutboxEventStore store;
  @Inject LegacyStoreGateway legacyStoreGateway;

  @Override
  public void notifyLegacyStore() {
    var pendingEvents = store.findPendingEvents();
    LOGGER.infof("Found %d pending outbox events to process", pendingEvents.size());

    pendingEvents.forEach(event -> {
      LOGGER.infof("Processing outbox event: eventId=%d, type=%s, storeName=%s",
          event.eventId(), event.eventType(), event.store().name());
      processEvent(event);
      store.processed(event);
      LOGGER.infof("Outbox event processed successfully: eventId=%d", event.eventId());
    });

    LOGGER.infof("Finished processing %d outbox events", pendingEvents.size());
  }

  private void processEvent(OutboxEvent event) {
    switch (event.eventType()) {
      case STORE_CREATED:
        LOGGER.infof("Notifying legacy system - STORE_CREATED: storeName=%s", event.store().name());
        legacyStoreGateway.createStoreOnLegacySystem(event.store());
        break;
      case STORE_UPDATED:
        LOGGER.infof("Notifying legacy system - STORE_UPDATED: storeName=%s", event.store().name());
        legacyStoreGateway.updateStoreOnLegacySystem(event.store());
        break;
      default:
        LOGGER.warnf("Unknown event type: %s for eventId=%d", event.eventType(), event.eventId());
    }
  }
}
