package com.fulfilment.application.monolith.stores.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UpdateStoreUseCaseTest {

  @Mock
  private StoreStore storeStore;

  @Mock
  private CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @InjectMocks
  private UpdateStoreUseCase updateStoreUseCase;

  private Store existingStore;
  private Store updatedStore;

  @BeforeEach
  void setUp() {
    existingStore = new Store("PAX", 30);
    updatedStore = new Store("PAX", 35);
  }

  @Test
  void update_WithExistingStore_ShouldUpdateAndCreateOutboxEvent() {
    // Given
    when(storeStore.findByName("PAX")).thenReturn(Optional.of(existingStore));

    // When
    updateStoreUseCase.update("PAX", updatedStore);

    // Then
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).update(storeCaptor.capture());

    Store capturedStore = storeCaptor.getValue();
    assertEquals("PAX", capturedStore.name());
    assertEquals(35, capturedStore.quantityProductsInStock());

    ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(createOutboxOutboxEventUseCase).create(eventCaptor.capture());

    OutboxEvent capturedEvent = eventCaptor.getValue();
    assertEquals("PAX", capturedEvent.store().name());
    assertEquals(35, capturedEvent.store().quantityProductsInStock());
    assertEquals(EventType.STORE_UPDATED, capturedEvent.eventType());
  }

  @Test
  void update_WithNonExistentStore_ShouldThrowNotFoundException() {
    // Given
    when(storeStore.findByName("NONEXISTENT")).thenReturn(Optional.empty());

    // When/Then
    assertThrows(StoreNotFoundException.class, () -> {
      updateStoreUseCase.update("NONEXISTENT", updatedStore);
    });

    verify(storeStore, never()).update(any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void update_ShouldUsePathParameterForLookup() {
    // Given
    when(storeStore.findByName("ORIGINAL")).thenReturn(Optional.of(existingStore));

    // When
    updateStoreUseCase.update("ORIGINAL", updatedStore);

    // Then
    verify(storeStore).findByName("ORIGINAL");
  }

  @Test
  void update_ShouldPreserveStoreNameFromPath() {
    // Given
    when(storeStore.findByName("KALLAX")).thenReturn(Optional.of(existingStore));
    Store updateData = new Store("DIFFERENT_NAME", 50);

    // When
    updateStoreUseCase.update("KALLAX", updateData);

    // Then
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).update(storeCaptor.capture());

    Store capturedStore = storeCaptor.getValue();
    assertEquals("KALLAX", capturedStore.name(), "Should use path parameter name, not body name");
    assertEquals(50, capturedStore.quantityProductsInStock());
  }

  @Test
  void update_ShouldCallStoreStoreBeforeOutbox() {
    // Given
    when(storeStore.findByName("PAX")).thenReturn(Optional.of(existingStore));
    var inOrder = inOrder(storeStore, createOutboxOutboxEventUseCase);

    // When
    updateStoreUseCase.update("PAX", updatedStore);

    // Then - verify order of operations
    inOrder.verify(storeStore).findByName("PAX");
    inOrder.verify(storeStore).update(any(Store.class));
    inOrder.verify(createOutboxOutboxEventUseCase).create(any(OutboxEvent.class));
  }
}
