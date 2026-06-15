package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.stores.adapters.database.DbOutboxEvent;
import com.fulfilment.application.monolith.stores.adapters.database.OutboxEventRepository;
import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.NotifyLegacyStoreOperation;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class OutboxEventIT {

  private static final String BASE_PATH = "/store";

  @Inject OutboxEventRepository outboxEventRepository;

  @Inject NotifyLegacyStoreOperation notifyLegacyStoreOperation;

  @Test
  @Transactional
  public void testOutboxEventCreatedWhenStoreIsCreated() {
    // Get initial count of outbox events
    long initialCount = outboxEventRepository.count();

    // Create a new store via REST API
    String createRequest =
        """
        {
          "name": "OUTBOX_TEST_CREATE",
          "quantityProductsInStock": 50
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Verify an outbox event was created
    long finalCount = outboxEventRepository.count();
    assertEquals(initialCount + 1, finalCount, "One outbox event should be created");

    // Find the created outbox event
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
    OutboxEvent createdEvent =
        pendingEvents.stream()
            .filter(e -> e.store().name().equals("OUTBOX_TEST_CREATE"))
            .findFirst()
            .orElse(null);

    assertNotNull(createdEvent, "Outbox event should exist for the created store");
    assertEquals(EventType.STORE_CREATED, createdEvent.eventType());
    assertEquals("OUTBOX_TEST_CREATE", createdEvent.store().name());
    assertEquals(50, createdEvent.store().quantityProductsInStock());
  }

  @Test
  @Transactional
  public void testOutboxEventCreatedWhenStoreIsUpdated() {
    // First create a store
    String createRequest =
        """
        {
          "name": "OUTBOX_TEST_UPDATE",
          "quantityProductsInStock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Get count before update
    long countBeforeUpdate = outboxEventRepository.count();

    // Update the store
    String updateRequest =
        """
        {
          "name": "OUTBOX_TEST_UPDATE",
          "quantityProductsInStock": 60
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put(BASE_PATH + "/OUTBOX_TEST_UPDATE")
        .then()
        .statusCode(204);

    // Verify an outbox event was created for the update
    long countAfterUpdate = outboxEventRepository.count();
    assertEquals(countBeforeUpdate + 1, countAfterUpdate, "One outbox event should be created for update");

    // Find the update event
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
    OutboxEvent updateEvent =
        pendingEvents.stream()
            .filter(e -> e.eventType() == EventType.STORE_UPDATED && e.store().name().equals("OUTBOX_TEST_UPDATE"))
            .findFirst()
            .orElse(null);

    assertNotNull(updateEvent, "Outbox event should exist for the updated store");
    assertEquals(EventType.STORE_UPDATED, updateEvent.eventType());
    assertEquals(60, updateEvent.store().quantityProductsInStock());
  }

  @Test
  @Transactional
  public void testFindPendingEvents() {
    // Create a few stores to generate outbox events
    String store1 =
        """
        {
          "name": "PENDING_TEST_1",
          "quantityProductsInStock": 10
        }
        """;

    String store2 =
        """
        {
          "name": "PENDING_TEST_2",
          "quantityProductsInStock": 20
        }
        """;

    given().contentType(ContentType.JSON).body(store1).when().post(BASE_PATH).then().statusCode(201);

    given().contentType(ContentType.JSON).body(store2).when().post(BASE_PATH).then().statusCode(201);

    // Find pending events
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();

    // Verify we have at least our 2 events
    long ourEventsCount =
        pendingEvents.stream()
            .filter(e -> e.store().name().startsWith("PENDING_TEST_"))
            .count();
    assertTrue(ourEventsCount >= 2, "Should have at least 2 pending events for our test stores");

    // Verify all pending events have the correct structure
    pendingEvents.stream()
        .filter(e -> e.store().name().startsWith("PENDING_TEST_"))
        .forEach(
            event -> {
              assertNotNull(event.eventId(), "Event should have an ID");
              assertNotNull(event.eventType(), "Event should have a type");
              assertNotNull(event.store(), "Event should have store data");
              assertNotNull(event.store().name(), "Store should have a name");
            });
  }

  @Test
  @Transactional
  public void testProcessOutboxEvents() {
    // Create a store to generate an outbox event
    String createRequest =
        """
        {
          "name": "PROCESS_TEST",
          "quantityProductsInStock": 25
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Find the pending event
    List<OutboxEvent> pendingEventsBefore = outboxEventRepository.findPendingEvents();
    OutboxEvent testEvent =
        pendingEventsBefore.stream()
            .filter(e -> e.store().name().equals("PROCESS_TEST"))
            .findFirst()
            .orElse(null);

    assertNotNull(testEvent, "Pending event should exist");

    // Get the DB entity to check status before processing
    DbOutboxEvent dbEventBefore = outboxEventRepository.findById(testEvent.eventId());
    assertEquals(DbOutboxEvent.ProcessingStatus.PENDING, dbEventBefore.status);
    assertNull(dbEventBefore.processedAt);

    // Process the outbox events
    notifyLegacyStoreOperation.notifyLegacyStore();

    // Verify the event is no longer pending
    List<OutboxEvent> pendingEventsAfter = outboxEventRepository.findPendingEvents();
    boolean stillPending =
        pendingEventsAfter.stream()
            .anyMatch(e -> e.eventId().equals(testEvent.eventId()));
    assertFalse(stillPending, "Event should no longer be pending after processing");

    // Verify the event was marked as processed in the database
    DbOutboxEvent dbEventAfter = outboxEventRepository.findById(testEvent.eventId());
    assertEquals(DbOutboxEvent.ProcessingStatus.PROCESSED, dbEventAfter.status);
    assertNotNull(dbEventAfter.processedAt, "processedAt should be set after processing");
  }

  @Test
  @Transactional
  public void testOutboxEventContainsCorrectStoreData() {
    // Create a store with specific data
    String createRequest =
        """
        {
          "name": "DATA_TEST_STORE",
          "quantityProductsInStock": 123
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Find the outbox event
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
    OutboxEvent event =
        pendingEvents.stream()
            .filter(e -> e.store().name().equals("DATA_TEST_STORE"))
            .findFirst()
            .orElse(null);

    assertNotNull(event, "Outbox event should exist");

    // Verify the event contains the exact store data
    Store storedData = event.store();
    assertEquals("DATA_TEST_STORE", storedData.name());
    assertEquals(123, storedData.quantityProductsInStock());
  }

  @Test
  @Transactional
  public void testMultipleEventsForSameStore() {
    // Create a store
    String createRequest =
        """
        {
          "name": "MULTIPLE_EVENTS",
          "quantityProductsInStock": 10
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    // Update the store multiple times
    String update1 =
        """
        {
          "name": "MULTIPLE_EVENTS",
          "quantityProductsInStock": 20
        }
        """;

    String update2 =
        """
        {
          "name": "MULTIPLE_EVENTS",
          "quantityProductsInStock": 30
        }
        """;

    given()
        .contentType(ContentType.JSON)
        .body(update1)
        .when()
        .put(BASE_PATH + "/MULTIPLE_EVENTS")
        .then()
        .statusCode(204);

    given()
        .contentType(ContentType.JSON)
        .body(update2)
        .when()
        .put(BASE_PATH + "/MULTIPLE_EVENTS")
        .then()
        .statusCode(204);

    // Find all events for this store
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents();
    List<OutboxEvent> storeEvents =
        pendingEvents.stream()
            .filter(e -> e.store().name().equals("MULTIPLE_EVENTS"))
            .toList();

    // Should have 3 events: 1 CREATE + 2 UPDATES
    assertEquals(3, storeEvents.size(), "Should have 3 events for the store");

    // Verify event types
    long createEvents =
        storeEvents.stream().filter(e -> e.eventType() == EventType.STORE_CREATED).count();
    long updateEvents =
        storeEvents.stream().filter(e -> e.eventType() == EventType.STORE_UPDATED).count();

    assertEquals(1, createEvents, "Should have 1 CREATE event");
    assertEquals(2, updateEvents, "Should have 2 UPDATE events");
  }
}