package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArchiveWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private WarehouseValidator warehouseValidator;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @BeforeEach
  void setUp() {
    warehouseValidator = new WarehouseValidator();
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore, warehouseValidator);
  }

  @Test
  void shouldArchiveWarehouseSuccessfully() {
    // Given
    Warehouse existingWarehouse = new Warehouse(
        "MWH.001",
        "AMSTERDAM-001",
        100,
        50,
        null,
        null // Active warehouse
    );

    Warehouse warehouseToArchive = new Warehouse("MWH.001", null, null, null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    archiveWarehouseUseCase.archive(warehouseToArchive);

    // Then
    verify(warehouseStore).findByBusinessUnitCode("MWH.001");
    verify(warehouseStore).archive(warehouseToArchive);
  }

  @Test
  void shouldFailWhenWarehouseNotFound() {
    // Given
    Warehouse warehouseToArchive = new Warehouse("MWH.999", null, null, null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.999"))
        .thenReturn(java.util.Optional.empty());

    // When & Then
    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class, () -> archiveWarehouseUseCase.archive(warehouseToArchive));

    assertEquals(
        "Warehouse with business unit code 'MWH.999' not found", exception.getMessage());
    verify(warehouseStore, never()).archive(any());
  }

  @Test
  void shouldFailWhenWarehouseAlreadyArchived() {
    // Given - archived warehouses are not returned by findByBusinessUnitCode (it only returns active ones)
    Warehouse warehouseToArchive = new Warehouse("MWH.001", null, null, null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.empty()); // Archived warehouse not found

    // When & Then
    WarehouseNotFoundException exception =
        assertThrows(
            WarehouseNotFoundException.class, () -> archiveWarehouseUseCase.archive(warehouseToArchive));

    assertEquals("Warehouse with business unit code 'MWH.001' not found", exception.getMessage());
    verify(warehouseStore, never()).archive(any());
  }

  @Test
  void shouldCallArchiveOnRepository() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.001", null, null, null, null, null);

    Warehouse warehouseToArchive = new Warehouse("MWH.001", null, null, null, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(java.util.Optional.of(existingWarehouse));

    // When
    archiveWarehouseUseCase.archive(warehouseToArchive);

    // Then - verify archive method is called on repository
    // Note: archivedAt timestamp is set by the repository, not the use case
    verify(warehouseStore).archive(warehouseToArchive);
  }

  @Test
  void shouldValidateBusinessUnitCode() {
    // Given
    Warehouse warehouseWithInvalidCode = new Warehouse("INVALID", null, null, null, null, null);

    // When & Then
    assertThrows(
        Exception.class, () -> archiveWarehouseUseCase.archive(warehouseWithInvalidCode));

    verify(warehouseStore, never()).archive(any());
  }
}