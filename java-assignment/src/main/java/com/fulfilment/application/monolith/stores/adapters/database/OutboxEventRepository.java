package com.fulfilment.application.monolith.stores.adapters.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.stores.domain.exceptions.OutboxSerializationException;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.OutboxEventStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class OutboxEventRepository implements OutboxEventStore, PanacheRepository<DbOutboxEvent> {

  private static final Logger LOG = Logger.getLogger(OutboxEventRepository.class);

  @Inject ObjectMapper objectMapper;

  @Override
  public void create(OutboxEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event.store());
      DbOutboxEvent dbOutboxEvent = new DbOutboxEvent(event.eventType(), payload);
      persist(dbOutboxEvent);
      LOG.infof("Outbox event persisted to database: type=%s, storeName=%s", event.eventType(), event.store().name());
    } catch (JsonProcessingException e) {
      LOG.errorf(e, "Failed to serialize store to JSON: storeName=%s", event.store().name());
      throw new OutboxSerializationException(event.store().name(), e);
    }
  }

  @Override
  public void processed(OutboxEvent event) {
    var entity = findById(event.eventId());
    entity.status = DbOutboxEvent.ProcessingStatus.PROCESSED;
    entity.processedAt = java.time.LocalDateTime.now();
    persist(entity);
    LOG.infof("Outbox event marked as PROCESSED: eventId=%d", event.eventId());
  }

  public List<OutboxEvent> findPendingEvents() {
    List<DbOutboxEvent> pendingEntities = list("status = ?1", DbOutboxEvent.ProcessingStatus.PENDING);
    List<OutboxEvent> pendingEvents = new ArrayList<>();
    int failedEvents = 0;

    for (DbOutboxEvent pendingEntity : pendingEntities) {
      if (tryMapToDomain(pendingEntity, pendingEvents)) {
        continue;
      }
      failedEvents++;
    }

    LOG.infof(
        "Retrieved %d pending outbox events from database (%d failed payload(s) marked as FAILED)",
        pendingEvents.size(),
        failedEvents);
    return pendingEvents;
  }

  private boolean tryMapToDomain(DbOutboxEvent dbOutboxEvent, List<OutboxEvent> targetEvents) {
    try {
      final var store = objectMapper.readValue(dbOutboxEvent.payload, Store.class);
      targetEvents.add(new OutboxEvent(store, dbOutboxEvent.eventType, dbOutboxEvent.id));
      return true;
    } catch (JsonProcessingException e) {
      LOG.errorf(e, "Failed to deserialize store from JSON: eventId=%d", dbOutboxEvent.id);
      dbOutboxEvent.status = DbOutboxEvent.ProcessingStatus.FAILED;
      dbOutboxEvent.processedAt = LocalDateTime.now();
      persist(dbOutboxEvent);
      return false;
    }
  }

}
