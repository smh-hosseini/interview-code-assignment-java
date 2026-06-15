package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;
  @Mock private CreateWarehouseOperation createWarehouseOperation;

  private WarehouseValidator warehouseValidator;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  void setUp() {
    warehouseValidator = new WarehouseValidator();
    replaceWarehouseUseCase =
        new ReplaceWarehouseUseCase(warehouseStore, createWarehouseOperation, warehouseValidator);
  }

  @Test
  void shouldReplaceWarehouseSuccessfully() {
    // Given
    Warehouse existingWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-001",
        100,
        50,
        null,
        null // Active warehouse
    );

    Warehouse newWarehouse = new Warehouse(
        "MWH.001", // Same business unit code
        "AMSTERDAM-002",
        120,
        50, // Must match existing stock
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    // Verify old warehouse was archived
    ArgumentCaptor<Warehouse> archivedWarehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(archivedWarehouseCaptor.capture());
    Warehouse archivedWarehouse = archivedWarehouseCaptor.getValue();
    assertNotNull(archivedWarehouse.archivedAt());
    assertEquals("MWH.001", archivedWarehouse.businessUnitCode());

    // Verify new warehouse was created
    verify(createWarehouseOperation).create(newWarehouse);
  }

  @Test
  void shouldFailWhenWarehouseNotFound() {
    // Given
    Warehouse newWarehouse = new Warehouse("MWH.999", "AMSTERDAM-001", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.999"))
        .thenReturn(java.util.Optional.empty());

    // When & Then
    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "Warehouse with business unit code 'MWH.999' not found", exception.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(createWarehouseOperation, never()).create(any());
  }

  @Test
  void shouldFailWhenNewCapacityLessThanExistingStock() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-002",
        40, // Less than existing stock (50)
        50,
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When & Then
    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals(
        "New capacity (40) cannot accommodate existing stock (50)", exception.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(createWarehouseOperation, never()).create(any());
  }

  @Test
  void shouldFailWhenStockDoesNotMatch() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-002",
        120,
        60, // Different from existing stock (50)
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When & Then
    WarehouseValidationException exception =
        assertThrows(
            WarehouseValidationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));

    assertEquals("Stock must match previous warehouse stock (50)", exception.getMessage());
    verify(warehouseStore, never()).update(any());
    verify(createWarehouseOperation, never()).create(any());
  }

  @Test
  void shouldReplaceWarehouseWithSameCapacity() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-002",
        100, // Same capacity
        50,
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).update(any());
    verify(createWarehouseOperation).create(newWarehouse);
  }

  @Test
  void shouldReplaceWarehouseWithCapacityEqualToStock() {
    // Given - Edge case: new capacity = existing stock
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-002",
        50, // Exactly equal to existing stock
        50,
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).update(any());
    verify(createWarehouseOperation).create(newWarehouse);
  }

  @Test
  void shouldReplaceWarehouseWithZeroStock() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 0, null, null); // Zero stock

    Warehouse newWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-002",
        120,
        0, // Must match
        null,
        null
    );

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    verify(warehouseStore).update(any());
    verify(createWarehouseOperation).create(newWarehouse);
  }

  @Test
  void shouldArchiveOldWarehouseBeforeCreatingNew() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse("MWH.001", "AMSTERDAM-002", 120, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then - Verify order of operations
    var inOrder = inOrder(warehouseStore, createWarehouseOperation);
    inOrder.verify(warehouseStore).update(any()); // Archive first
    inOrder.verify(createWarehouseOperation).create(any()); // Create second
  }

  @Test
  void shouldSetArchivedAtTimestampWhenReplacing() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    Warehouse newWarehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 120, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    java.time.LocalDateTime beforeReplace = java.time.LocalDateTime.now();
    replaceWarehouseUseCase.replace(newWarehouse);
    java.time.LocalDateTime afterReplace = java.time.LocalDateTime.now();

    // Then
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseStore).update(warehouseCaptor.capture());

    Warehouse archivedWarehouse = warehouseCaptor.getValue();
    assertNotNull(archivedWarehouse.archivedAt());
    assertTrue(
        archivedWarehouse.archivedAt().isAfter(beforeReplace)
            || archivedWarehouse.archivedAt().isEqual(beforeReplace));
    assertTrue(
        archivedWarehouse.archivedAt().isBefore(afterReplace)
            || archivedWarehouse.archivedAt().isEqual(afterReplace));
  }
}