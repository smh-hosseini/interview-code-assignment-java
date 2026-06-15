package com.fulfilment.application.monolith.stores.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class PatchStoreUseCaseTest {

  @Mock
  private StoreStore storeStore;

  @Mock
  private CreateOutboxOutboxEventUseCase createOutboxOutboxEventUseCase;

  @InjectMocks
  private PatchStoreUseCase patchStoreUseCase;

  private Store existingStore;

  @BeforeEach
  void setUp() {
    existingStore = new Store("IVAR", 15);
  }

  @Test
  void patch_WithQuantityOnly_ShouldUpdateQuantityKeepName() {
    // Given
    when(storeStore.findByName("IVAR")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store(null, 25);

    // When
    patchStoreUseCase.patch("IVAR", patchData);

    // Then
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).patch(nameCaptor.capture(), storeCaptor.capture());

    assertEquals("IVAR", nameCaptor.getValue());
    Store patchedStore = storeCaptor.getValue();
    assertEquals("IVAR", patchedStore.name());
    assertEquals(25, patchedStore.quantityProductsInStock());
  }

  @Test
  void patch_WithNameOnly_ShouldUpdateNameKeepQuantity() {
    // Given
    when(storeStore.findByName("LACK")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store("LACK-UPDATED", null);

    // When
    patchStoreUseCase.patch("LACK", patchData);

    // Then
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).patch(nameCaptor.capture(), storeCaptor.capture());

    assertEquals("LACK", nameCaptor.getValue());
    Store patchedStore = storeCaptor.getValue();
    assertEquals("LACK-UPDATED", patchedStore.name());
    assertEquals(15, patchedStore.quantityProductsInStock());
  }

  @Test
  void patch_WithBothFields_ShouldUpdateBoth() {
    // Given
    when(storeStore.findByName("NORDLI")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store("NORDLI-NEW", 30);

    // When
    patchStoreUseCase.patch("NORDLI", patchData);

    // Then
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).patch(nameCaptor.capture(), storeCaptor.capture());

    assertEquals("NORDLI", nameCaptor.getValue());
    Store patchedStore = storeCaptor.getValue();
    assertEquals("NORDLI-NEW", patchedStore.name());
    assertEquals(30, patchedStore.quantityProductsInStock());
  }

  @Test
  void patch_WithZeroQuantity_ShouldKeepExistingQuantity() {
    // Given
    when(storeStore.findByName("EKTORP")).thenReturn(Optional.of(new Store("EKTORP", 12)));
    Store patchData = new Store(null, 0);

    // When
    patchStoreUseCase.patch("EKTORP", patchData);

    // Then
    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    verify(storeStore).patch(eq("EKTORP"), storeCaptor.capture());

    Store patchedStore = storeCaptor.getValue();
    assertEquals(12, patchedStore.quantityProductsInStock(), "Zero quantity should be ignored");
  }

  @Test
  void patch_WithNonExistentStore_ShouldThrowNotFoundException() {
    // Given
    when(storeStore.findByName("NONEXISTENT")).thenReturn(Optional.empty());
    Store patchData = new Store(null, 50);

    // When/Then
    assertThrows(StoreNotFoundException.class, () -> {
      patchStoreUseCase.patch("NONEXISTENT", patchData);
    });

    verify(storeStore, never()).patch(any(), any());
    verify(createOutboxOutboxEventUseCase, never()).create(any());
  }

  @Test
  void patch_ShouldCreateOutboxEventWithMergedValues() {
    // Given
    when(storeStore.findByName("IVAR")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store("IVAR-UPDATED", 25);

    // When
    patchStoreUseCase.patch("IVAR", patchData);

    // Then
    ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
    verify(createOutboxOutboxEventUseCase).create(eventCaptor.capture());

    OutboxEvent capturedEvent = eventCaptor.getValue();
    assertEquals("IVAR-UPDATED", capturedEvent.store().name());
    assertEquals(25, capturedEvent.store().quantityProductsInStock());
    assertEquals(EventType.STORE_UPDATED, capturedEvent.eventType());
  }

  @Test
  void patch_ShouldUseOriginalNameForLookup() {
    // Given
    when(storeStore.findByName("ORIGINAL")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store("UPDATED", 100);

    // When
    patchStoreUseCase.patch("ORIGINAL", patchData);

    // Then
    verify(storeStore).findByName("ORIGINAL");
    verify(storeStore).patch(eq("ORIGINAL"), any(Store.class));
  }

  @Test
  void patch_ShouldCallStoreStoreBeforeOutbox() {
    // Given
    when(storeStore.findByName("IVAR")).thenReturn(Optional.of(existingStore));
    Store patchData = new Store(null, 25);
    var inOrder = inOrder(storeStore, createOutboxOutboxEventUseCase);

    // When
    patchStoreUseCase.patch("IVAR", patchData);

    // Then - verify order of operations
    inOrder.verify(storeStore).findByName("IVAR");
    inOrder.verify(storeStore).patch(eq("IVAR"), any(Store.class));
    inOrder.verify(createOutboxOutboxEventUseCase).create(any(OutboxEvent.class));
  }
}
