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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FindStoreUseCaseTest {

  @Mock
  private StoreStore storeStore;

  @InjectMocks
  private FindStoreUseCase findStoreUseCase;

  @Test
  void findAll_ShouldReturnAllStores() {
    // Given
    List<Store> expectedStores = Arrays.asList(
        new Store("TONSTAD", 10),
        new Store("KALLAX", 5),
        new Store("BESTÅ", 3)
    );
    when(storeStore.findAllStores()).thenReturn(expectedStores);

    // When
    List<Store> result = findStoreUseCase.findAll();

    // Then
    assertEquals(3, result.size());
    assertEquals(expectedStores, result);
    verify(storeStore).findAllStores();
  }

  @Test
  void findAll_WithNoStores_ShouldReturnEmptyList() {
    // Given
    when(storeStore.findAllStores()).thenReturn(List.of());

    // When
    List<Store> result = findStoreUseCase.findAll();

    // Then
    assertTrue(result.isEmpty());
    verify(storeStore).findAllStores();
  }

  @Test
  void findByName_WithExistingStore_ShouldReturnStore() {
    // Given
    Store expectedStore = new Store("HEMNES", 25);
    when(storeStore.findByName("HEMNES")).thenReturn(Optional.of(expectedStore));

    // When
    Store result = findStoreUseCase.findByName("HEMNES");

    // Then
    assertNotNull(result);
    assertEquals("HEMNES", result.name());
    assertEquals(25, result.quantityProductsInStock());
    verify(storeStore).findByName("HEMNES");
  }

  @Test
  void findByName_WithNonExistentStore_ShouldThrowNotFoundException() {
    // Given
    when(storeStore.findByName("NONEXISTENT")).thenReturn(Optional.empty());

    // When/Then
    assertThrows(StoreNotFoundException.class, () -> {
      findStoreUseCase.findByName("NONEXISTENT");
    });

    verify(storeStore).findByName("NONEXISTENT");
  }

  @Test
  void findByName_ShouldPassNameToRepository() {
    // Given
    Store store = new Store("BILLY", 0);
    when(storeStore.findByName("BILLY")).thenReturn(Optional.of(store));

    // When
    findStoreUseCase.findByName("BILLY");

    // Then
    verify(storeStore).findByName("BILLY");
  }
}
