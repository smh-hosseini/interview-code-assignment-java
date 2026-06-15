package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseAlreadyExistsException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.validation.WarehouseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;
  @Mock private LocationResolver locationResolver;

  private WarehouseValidator warehouseValidator;
  private CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  void setUp() {
    warehouseValidator = new WarehouseValidator();
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseRepository, locationResolver, warehouseValidator);
  }

  @Test
  void shouldCreateWarehouseSuccessfully() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 50, 30, null, null);

    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.sumActiveCapacityByLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository).create(warehouseCaptor.capture());

    Warehouse createdWarehouse = warehouseCaptor.getValue();
    assertEquals("MWH.100", createdWarehouse.businessUnitCode());
    assertEquals("AMSTERDAM-001", createdWarehouse.location());
    assertEquals(50, createdWarehouse.capacity());
    assertEquals(30, createdWarehouse.stock());
  }

  @Test
  void shouldFailWhenBusinessUnitCodeAlreadyExists() {
    // Given
    Warehouse existingWarehouse = new Warehouse("MWH.100", null, null, null, null, null); // Active warehouse

    Warehouse newWarehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 50, 30, null, null);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.of(existingWarehouse));

    // When & Then
    WarehouseAlreadyExistsException exception =
        assertThrows(
            WarehouseAlreadyExistsException.class, () -> createWarehouseUseCase.create(newWarehouse));

    assertEquals(
        "Warehouse with business unit code 'MWH.100' already exists", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldAllowCreationIfPreviousWarehouseIsArchived() {
    // Given
    Warehouse archivedWarehouse = new Warehouse(
        "MWH.100",
        null,
        null,
        null,
        null,
        java.time.LocalDateTime.now() // Archived
    );

    Warehouse newWarehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 50, 30, null, null);

    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.of(archivedWarehouse));
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.sumActiveCapacityByLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(newWarehouse);

    // Then
    verify(warehouseRepository).create(any());
  }

  @Test
  void shouldFailWhenLocationDoesNotExist() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "INVALID-LOCATION", 50, 30, null, null);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("INVALID-LOCATION")).thenReturn(null);

    // When & Then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals("Location 'INVALID-LOCATION' does not exist", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldFailWhenMaxNumberOfWarehousesReached() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "ZWOLLE-001", 30, 20, null, null);

    Location location = new Location("ZWOLLE-001", 1, 40); // Max 1 warehouse

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("ZWOLLE-001")).thenReturn(1L); // Already 1

    // When & Then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Maximum number of warehouses (1) reached at location 'ZWOLLE-001'",
        exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldFailWhenTotalCapacityExceedsLocationMax() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "ZWOLLE-001", 30, 20, null, null);

    Location location = new Location("ZWOLLE-001", 5, 40); // Max capacity 40

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("ZWOLLE-001")).thenReturn(0L);
    when(warehouseRepository.sumActiveCapacityByLocation("ZWOLLE-001"))
        .thenReturn(20); // Current: 20, new: 30 = 50 > 40

    // When & Then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Total capacity would exceed location maximum of 40", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldFailWhenCapacityLessThanStock() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 30, 50, null, null); // Stock > Capacity

    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.sumActiveCapacityByLocation("AMSTERDAM-001")).thenReturn(40);

    // When & Then
    WarehouseValidationException exception =
        assertThrows(WarehouseValidationException.class, () -> createWarehouseUseCase.create(warehouse));

    assertEquals(
        "Warehouse capacity (30) cannot be less than stock (50)", exception.getMessage());
    verify(warehouseRepository, never()).create(any());
  }

  @Test
  void shouldCreateWarehouseWithZeroStock() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 50, 0, null, null);

    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.sumActiveCapacityByLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository).create(warehouseCaptor.capture());
  }

  @Test
  void shouldCreateWarehouseWhenCapacityEqualsStock() {
    // Given
    Warehouse warehouse = new Warehouse("MWH.100", "AMSTERDAM-001", 50, 50, null, null); // Equal

    Location location = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseRepository.findByBusinessUnitCode("MWH.100")).thenReturn(Optional.empty());
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(location);
    when(warehouseRepository.countActiveWarehousesByLocation("AMSTERDAM-001")).thenReturn(2L);
    when(warehouseRepository.sumActiveCapacityByLocation("AMSTERDAM-001")).thenReturn(40);

    // When
    createWarehouseUseCase.create(warehouse);

    // Then
    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(warehouseRepository).create(warehouseCaptor.capture());
  }
}