package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentValidationException;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CreateFulfillmentUseCaseTest {

  @Mock private FulfillmentStore fulfillmentStore;
  @Mock private WarehouseStore warehouseStore;

  private CreateFulfillmentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateFulfillmentUseCase(fulfillmentStore, warehouseStore);
  }

  @Test
  void shouldCreateFulfillmentSuccessfully() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.001", "1", "1", null, null);

    Warehouse warehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(Optional.of(warehouse));
    when(fulfillmentStore.countWarehousesByProductAndStore("1", "1")).thenReturn(0L);
    when(fulfillmentStore.countWarehousesByStore("1")).thenReturn(0L);
    when(fulfillmentStore.countProductsByWarehouse("MWH.001")).thenReturn(0L);

    // When
    useCase.create(fulfillment);

    // Then
    ArgumentCaptor<WarehouseFulfillment> captor =
        ArgumentCaptor.forClass(WarehouseFulfillment.class);
    verify(fulfillmentStore).create(captor.capture());

    WarehouseFulfillment created = captor.getValue();
    assertEquals("MWH.001", created.warehouseBusinessUnitCode());
    assertEquals("1", created.productId());
    assertEquals("1", created.storeId());
    assertEquals(1, created.priority()); // First warehouse = priority 1
  }

  @Test
  void shouldSetSecondaryPriorityForSecondWarehouse() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.002", "1", "1", null, null);

    Warehouse warehouse = new Warehouse("MWH.002", "AMSTERDAM-002", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.002")).thenReturn(Optional.of(warehouse));
    when(fulfillmentStore.countWarehousesByProductAndStore("1", "1"))
        .thenReturn(1L); // Already has 1 warehouse
    when(fulfillmentStore.countWarehousesByStore("1")).thenReturn(1L);
    when(fulfillmentStore.countProductsByWarehouse("MWH.002")).thenReturn(0L);

    // When
    useCase.create(fulfillment);

    // Then
    ArgumentCaptor<WarehouseFulfillment> captor =
        ArgumentCaptor.forClass(WarehouseFulfillment.class);
    verify(fulfillmentStore).create(captor.capture());

    WarehouseFulfillment created = captor.getValue();
    assertEquals(2, created.priority()); // Second warehouse = priority 2
  }

  @Test
  void shouldFailWhenWarehouseNotFound() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.999", "1", "1", null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(Optional.empty());

    // When & Then
    FulfillmentValidationException exception =
        assertThrows(FulfillmentValidationException.class, () -> useCase.create(fulfillment));

    assertTrue(exception.getMessage().contains("Warehouse 'MWH.999' not found"));
    verify(fulfillmentStore, never()).create(any());
  }

  @Test
  void shouldFailWhenWarehouseIsArchived() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.001", "1", "1", null, null);

    Warehouse archivedWarehouse =
        new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, java.time.LocalDateTime.now());

    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(Optional.of(archivedWarehouse));

    // When & Then
    FulfillmentValidationException exception =
        assertThrows(FulfillmentValidationException.class, () -> useCase.create(fulfillment));

    assertTrue(exception.getMessage().contains("Cannot associate archived warehouse"));
    verify(fulfillmentStore, never()).create(any());
  }

  @Test
  void shouldFailWhenProductExceedsMaxWarehousesPerStore() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.001", "1", "1", null, null);

    Warehouse warehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(Optional.of(warehouse));
    when(fulfillmentStore.countWarehousesByProductAndStore("1", "1"))
        .thenReturn(2L); // Already has 2 warehouses

    // When & Then
    FulfillmentValidationException exception =
        assertThrows(FulfillmentValidationException.class, () -> useCase.create(fulfillment));

    assertTrue(exception.getMessage().contains("maximum (2) warehouses"));
    verify(fulfillmentStore, never()).create(any());
  }

  @Test
  void shouldFailWhenStoreExceedsMaxWarehouses() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.001", "1", "1", null, null);

    Warehouse warehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(Optional.of(warehouse));
    when(fulfillmentStore.countWarehousesByProductAndStore("1", "1")).thenReturn(0L);
    when(fulfillmentStore.countWarehousesByStore("1")).thenReturn(3L); // Already has 3 warehouses

    // When & Then
    FulfillmentValidationException exception =
        assertThrows(FulfillmentValidationException.class, () -> useCase.create(fulfillment));

    assertTrue(exception.getMessage().contains("maximum (3) fulfillment warehouses"));
    verify(fulfillmentStore, never()).create(any());
  }

  @Test
  void shouldFailWhenWarehouseExceedsMaxProducts() {
    // Given
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.001", "1", "1", null, null);

    Warehouse warehouse = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 50, null, null);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(Optional.of(warehouse));
    when(fulfillmentStore.countWarehousesByProductAndStore("1", "1")).thenReturn(0L);
    when(fulfillmentStore.countWarehousesByStore("1")).thenReturn(0L);
    when(fulfillmentStore.countProductsByWarehouse("MWH.001"))
        .thenReturn(5L); // Already has 5 products

    // When & Then
    FulfillmentValidationException exception =
        assertThrows(FulfillmentValidationException.class, () -> useCase.create(fulfillment));

    assertTrue(exception.getMessage().contains("maximum (5) product types"));
    verify(fulfillmentStore, never()).create(any());
  }
}
