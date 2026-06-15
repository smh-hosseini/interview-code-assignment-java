package com.fulfilment.application.monolith.stores.adapters.in;

import com.fulfilment.application.monolith.stores.domain.ports.NotifyLegacyStoreOperation;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OutboxProcessor {

  private static final Logger LOGGER = Logger.getLogger(OutboxProcessor.class.getName());

  @Inject
  NotifyLegacyStoreOperation notifyLegacyStoreOperation;

  @Scheduled(every = "10s")
  @Transactional
  public void processOutboxEvents() {
    LOGGER.info("Start processing pending outbox events.");
    notifyLegacyStoreOperation.notifyLegacyStore();
    LOGGER.info("Processing finished.");
  }
}