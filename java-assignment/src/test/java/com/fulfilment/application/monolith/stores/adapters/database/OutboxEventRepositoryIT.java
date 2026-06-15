package com.fulfilment.application.monolith.stores.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OutboxEventRepositoryIT {

  @Inject OutboxEventRepository outboxEventRepository;

  @Test
  @TestTransaction
  void createShouldPersistPendingOutboxEvent() {
    OutboxEvent event = new OutboxEvent(new Store("OUTBOX_REPO_CREATE", 10), EventType.STORE_CREATED, null);

    outboxEventRepository.create(event);
    var pendingEvents = outboxEventRepository.findPendingEvents();

    var created =
        pendingEvents.stream()
            .filter(pending -> "OUTBOX_REPO_CREATE".equals(pending.store().name()))
            .findFirst();

    assertTrue(created.isPresent());
    assertEquals(EventType.STORE_CREATED, created.get().eventType());
    assertNotNull(created.get().eventId());
  }

  @Test
  @TestTransaction
  void processedShouldMarkEventAsProcessed() {
    OutboxEvent event = new OutboxEvent(new Store("OUTBOX_REPO_PROCESS", 8), EventType.STORE_UPDATED, null);
    outboxEventRepository.create(event);

    var created =
        outboxEventRepository.findPendingEvents().stream()
            .filter(pending -> "OUTBOX_REPO_PROCESS".equals(pending.store().name()))
            .findFirst()
            .orElseThrow();

    outboxEventRepository.processed(created);

    assertTrue(
        outboxEventRepository.findPendingEvents().stream()
            .noneMatch(pending -> created.eventId().equals(pending.eventId())));

    DbOutboxEvent storedEntity = outboxEventRepository.findById(created.eventId());
    assertEquals(DbOutboxEvent.ProcessingStatus.PROCESSED, storedEntity.status);
    assertNotNull(storedEntity.processedAt);
  }

  @Test
  @TestTransaction
  void findPendingEventsShouldMarkRowsWithInvalidPayloadAsFailed() {
    DbOutboxEvent invalidJsonEvent = new DbOutboxEvent(EventType.STORE_CREATED, "{invalid-json");
    outboxEventRepository.persist(invalidJsonEvent);
    outboxEventRepository.flush();

    var pendingEvents = outboxEventRepository.findPendingEvents();

    assertTrue(
        pendingEvents.stream()
            .noneMatch(event -> invalidJsonEvent.id.equals(event.eventId())));

    DbOutboxEvent storedEntity = outboxEventRepository.findById(invalidJsonEvent.id);
    assertEquals(DbOutboxEvent.ProcessingStatus.FAILED, storedEntity.status);
    assertNotNull(storedEntity.processedAt);
  }
}
