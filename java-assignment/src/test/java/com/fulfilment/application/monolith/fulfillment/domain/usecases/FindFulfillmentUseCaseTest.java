package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FindFulfillmentUseCaseTest {

  @Mock
  private FulfillmentStore fulfillmentStore;

  private FindFulfillmentUseCase findFulfillmentUseCase;

  private WarehouseFulfillment fulfillment1;
  private WarehouseFulfillment fulfillment2;
  private WarehouseFulfillment fulfillment3;

  @BeforeEach
  void setUp() {
    findFulfillmentUseCase = new FindFulfillmentUseCase(fulfillmentStore);

    LocalDateTime now = LocalDateTime.now();
    fulfillment1 = new WarehouseFulfillment("MWH.001", "PRODUCT-1", "STORE-1", 1, now);
    fulfillment2 = new WarehouseFulfillment("MWH.002", "PRODUCT-1", "STORE-1", 2, now);
    fulfillment3 = new WarehouseFulfillment("MWH.001", "PRODUCT-2", "STORE-2", 1, now);
  }

  // ========== findByStore Tests ==========

  @Test
  void findByStore_WithExistingFulfillments_ShouldReturnList() {
    // Given
    List<WarehouseFulfillment> expectedFulfillments = Arrays.asList(fulfillment1, fulfillment2);
    when(fulfillmentStore.findByStore("STORE-1")).thenReturn(expectedFulfillments);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByStore("STORE-1");

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(expectedFulfillments, result);
    verify(fulfillmentStore).findByStore("STORE-1");
  }

  @Test
  void findByStore_WithNoFulfillments_ShouldReturnEmptyList() {
    // Given
    when(fulfillmentStore.findByStore("STORE-EMPTY")).thenReturn(List.of());

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByStore("STORE-EMPTY");

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(fulfillmentStore).findByStore("STORE-EMPTY");
  }

  @Test
  void findByStore_ShouldPassCorrectStoreId() {
    // Given
    when(fulfillmentStore.findByStore("HEMNES")).thenReturn(List.of());

    // When
    findFulfillmentUseCase.findByStore("HEMNES");

    // Then
    verify(fulfillmentStore).findByStore("HEMNES");
  }

  @Test
  void findByStore_WithMultipleFulfillments_ShouldReturnAll() {
    // Given
    List<WarehouseFulfillment> multipleFulfillments = Arrays.asList(
        fulfillment1, fulfillment2, fulfillment3
    );
    when(fulfillmentStore.findByStore("STORE-MULTI")).thenReturn(multipleFulfillments);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByStore("STORE-MULTI");

    // Then
    assertEquals(3, result.size());
    assertTrue(result.containsAll(multipleFulfillments));
  }

  // ========== findByWarehouse Tests ==========

  @Test
  void findByWarehouse_WithExistingFulfillments_ShouldReturnList() {
    // Given
    List<WarehouseFulfillment> expectedFulfillments = Arrays.asList(fulfillment1, fulfillment3);
    when(fulfillmentStore.findByWarehouse("MWH.001")).thenReturn(expectedFulfillments);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByWarehouse("MWH.001");

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(expectedFulfillments, result);
    verify(fulfillmentStore).findByWarehouse("MWH.001");
  }

  @Test
  void findByWarehouse_WithNoFulfillments_ShouldReturnEmptyList() {
    // Given
    when(fulfillmentStore.findByWarehouse("MWH.999")).thenReturn(List.of());

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByWarehouse("MWH.999");

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(fulfillmentStore).findByWarehouse("MWH.999");
  }

  @Test
  void findByWarehouse_ShouldPassCorrectWarehouseCode() {
    // Given
    when(fulfillmentStore.findByWarehouse("MWH.012")).thenReturn(List.of());

    // When
    findFulfillmentUseCase.findByWarehouse("MWH.012");

    // Then
    verify(fulfillmentStore).findByWarehouse("MWH.012");
  }

  @Test
  void findByWarehouse_WithSingleFulfillment_ShouldReturnSingletonList() {
    // Given
    List<WarehouseFulfillment> singleFulfillment = List.of(fulfillment1);
    when(fulfillmentStore.findByWarehouse("MWH.100")).thenReturn(singleFulfillment);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByWarehouse("MWH.100");

    // Then
    assertEquals(1, result.size());
    assertEquals(fulfillment1, result.get(0));
  }

  // ========== findByProductAndStore Tests ==========

  @Test
  void findByProductAndStore_WithExistingFulfillments_ShouldReturnList() {
    // Given
    List<WarehouseFulfillment> expectedFulfillments = Arrays.asList(fulfillment1, fulfillment2);
    when(fulfillmentStore.findByProductAndStore("PRODUCT-1", "STORE-1"))
        .thenReturn(expectedFulfillments);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase
        .findByProductAndStore("PRODUCT-1", "STORE-1");

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(expectedFulfillments, result);
    verify(fulfillmentStore).findByProductAndStore("PRODUCT-1", "STORE-1");
  }

  @Test
  void findByProductAndStore_WithNoFulfillments_ShouldReturnEmptyList() {
    // Given
    when(fulfillmentStore.findByProductAndStore("PRODUCT-NONE", "STORE-NONE"))
        .thenReturn(List.of());

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase
        .findByProductAndStore("PRODUCT-NONE", "STORE-NONE");

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(fulfillmentStore).findByProductAndStore("PRODUCT-NONE", "STORE-NONE");
  }

  @Test
  void findByProductAndStore_ShouldPassCorrectParameters() {
    // Given
    when(fulfillmentStore.findByProductAndStore("KALLAX", "HEMNES"))
        .thenReturn(List.of());

    // When
    findFulfillmentUseCase.findByProductAndStore("KALLAX", "HEMNES");

    // Then
    verify(fulfillmentStore).findByProductAndStore("KALLAX", "HEMNES");
  }

  @Test
  void findByProductAndStore_WithSingleMatch_ShouldReturnOne() {
    // Given
    List<WarehouseFulfillment> singleMatch = List.of(fulfillment1);
    when(fulfillmentStore.findByProductAndStore("PRODUCT-1", "STORE-1"))
        .thenReturn(singleMatch);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase
        .findByProductAndStore("PRODUCT-1", "STORE-1");

    // Then
    assertEquals(1, result.size());
    assertEquals("PRODUCT-1", result.get(0).productId());
    assertEquals("STORE-1", result.get(0).storeId());
  }

  @Test
  void findByProductAndStore_WithDifferentPriorities_ShouldReturnOrdered() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    WarehouseFulfillment lowPriority = new WarehouseFulfillment(
        "MWH.001", "PRODUCT-A", "STORE-A", 2, now
    );
    WarehouseFulfillment highPriority = new WarehouseFulfillment(
        "MWH.002", "PRODUCT-A", "STORE-A", 1, now
    );

    List<WarehouseFulfillment> expectedOrder = Arrays.asList(highPriority, lowPriority);
    when(fulfillmentStore.findByProductAndStore("PRODUCT-A", "STORE-A"))
        .thenReturn(expectedOrder);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase
        .findByProductAndStore("PRODUCT-A", "STORE-A");

    // Then
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).priority());
    assertEquals(2, result.get(1).priority());
  }

  // ========== Edge Cases ==========

  @Test
  void findByStore_WithNullResult_ShouldHandleGracefully() {
    // Given
    when(fulfillmentStore.findByStore("STORE-NULL")).thenReturn(null);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByStore("STORE-NULL");

    // Then
    assertNull(result);
  }

  @Test
  void findByWarehouse_WithNullResult_ShouldHandleGracefully() {
    // Given
    when(fulfillmentStore.findByWarehouse("MWH.NULL")).thenReturn(null);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase.findByWarehouse("MWH.NULL");

    // Then
    assertNull(result);
  }

  @Test
  void findByProductAndStore_WithNullResult_ShouldHandleGracefully() {
    // Given
    when(fulfillmentStore.findByProductAndStore("NULL-PROD", "NULL-STORE"))
        .thenReturn(null);

    // When
    List<WarehouseFulfillment> result = findFulfillmentUseCase
        .findByProductAndStore("NULL-PROD", "NULL-STORE");

    // Then
    assertNull(result);
  }
}
