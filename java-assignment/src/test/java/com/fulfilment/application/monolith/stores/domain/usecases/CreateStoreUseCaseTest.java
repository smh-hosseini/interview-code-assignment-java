package com.fulfilment.application.monolith.stores.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreValidationException;
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

@ExtendWith(MockitoExtension.class)
class CreateStoreUseCaseTest {

  @Mock
  private StoreStore storeStore;

  @Mock
  private CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @InjectMocks
  private CreateStoreUseCase createStoreUseCase;

  private Store validStore;

  @BeforeEach
  void setUp() {
    validStore = new Store("HEMNES", 25);
  }

  @Test
  void create_WithValidStore_ShouldCreateStoreAndOutboxEvent() {
    // When
    createStoreUseCase.create(validStore);

    // Then
    verify(storeStore).create(validStore);

    ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(createOutboxOutboxEventUseCase).create(eventCaptor.capture());

    OutboxEvent capturedEvent = eventCaptor.getValue();
    assertEquals(validStore, capturedEvent.store());
    assertEquals(EventType.STORE_CREATED, capturedEvent.eventType());
  }

  @Test
  void create_WithNullName_ShouldThrowValidationException() {
    // Given
    Store invalidStore = new Store(null, 10);

    // When/Then
    assertThrows(StoreValidationException.class, () -> {
      createStoreUseCase.create(invalidStore);
    });

    verify(storeStore, never()).create(any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void create_WithBlankName_ShouldThrowValidationException() {
    // Given
    Store invalidStore = new Store("   ", 10);

    // When/Then
    assertThrows(StoreValidationException.class, () -> {
      createStoreUseCase.create(invalidStore);
    });

    verify(storeStore, never()).create(any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void create_WithNameExceeding40Characters_ShouldThrowValidationException() {
    // Given
    Store invalidStore = new Store("A".repeat(41), 10);

    // When/Then
    assertThrows(StoreValidationException.class, () -> {
      createStoreUseCase.create(invalidStore);
    });

    verify(storeStore, never()).create(any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void create_WithNegativeQuantity_ShouldThrowValidationException() {
    // Given
    Store invalidStore = new Store("MALM", -5);

    // When/Then
    assertThrows(StoreValidationException.class, () -> {
      createStoreUseCase.create(invalidStore);
    });

    verify(storeStore, never()).create(any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void create_WithZeroQuantity_ShouldSucceed() {
    // Given
    Store storeWithZeroStock = new Store("BILLY", 0);

    // When
    createStoreUseCase.create(storeWithZeroStock);

    // Then
    verify(storeStore).create(storeWithZeroStock);
    verify(createOutboxOutboxEventUseCase).create(any(OutboxEvent.class));
  }

  @Test
  void create_ShouldCallStoreStoreBeforeOutbox() {
    // Given
    var inOrder = inOrder(storeStore, createOutboxOutboxEventUseCase);

    // When
    createStoreUseCase.create(validStore);

    // Then - verify order of operations
    inOrder.verify(storeStore).create(validStore);
    inOrder.verify(createOutboxOutboxEventUseCase).create(any(OutboxEvent.class));
  }
}