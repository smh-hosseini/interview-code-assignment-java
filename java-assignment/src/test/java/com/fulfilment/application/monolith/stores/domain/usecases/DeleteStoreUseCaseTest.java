package com.fulfilment.application.monolith.stores.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DeleteStoreUseCaseTest {

  @Mock
  private StoreStore storeStore;

  @InjectMocks
  private DeleteStoreUseCase deleteStoreUseCase;

  @Test
  void remove_WithExistingStore_ShouldDeleteStore() {
    // Given
    Store existingStore = new Store("FJÄLLBO", 8);
    when(storeStore.findByName("FJÄLLBO")).thenReturn(Optional.of(existingStore));

    // When
    deleteStoreUseCase.remove("FJÄLLBO");

    // Then
    verify(storeStore).findByName("FJÄLLBO");
    verify(storeStore).delete("FJÄLLBO");
  }

  @Test
  void remove_WithNonExistentStore_ShouldThrowNotFoundException() {
    // Given
    when(storeStore.findByName("NONEXISTENT")).thenReturn(Optional.empty());

    // When/Then
    assertThrows(StoreNotFoundException.class, () -> {
      deleteStoreUseCase.remove("NONEXISTENT");
    });

    verify(storeStore).findByName("NONEXISTENT");
    verify(storeStore, never()).delete(any());
  }

  @Test
  void remove_ShouldCallFindBeforeDelete() {
    // Given
    Store existingStore = new Store("BESTÅ", 3);
    when(storeStore.findByName("BESTÅ")).thenReturn(Optional.of(existingStore));
    var inOrder = inOrder(storeStore);

    // When
    deleteStoreUseCase.remove("BESTÅ");

    // Then - verify order of operations
    inOrder.verify(storeStore).findByName("BESTÅ");
    inOrder.verify(storeStore).delete("BESTÅ");
  }
}
