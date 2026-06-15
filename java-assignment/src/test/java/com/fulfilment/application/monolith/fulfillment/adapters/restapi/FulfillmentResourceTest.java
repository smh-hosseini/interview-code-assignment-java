package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.CreateFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FindFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.RemoveFulfillmentOperation;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FulfillmentResourceTest {

  @Mock private CreateFulfillmentOperation createFulfillmentOperation;
  @Mock private RemoveFulfillmentOperation removeFulfillmentOperation;
  @Mock private FindFulfillmentOperation findFulfillmentOperation;

  private FulfillmentResource resource;

  @BeforeEach
  void setUp() {
    resource = new FulfillmentResource();
    resource.createFulfillmentOperation = createFulfillmentOperation;
    resource.removeFulfillmentOperation = removeFulfillmentOperation;
    resource.findFulfillmentOperation = findFulfillmentOperation;
  }

  @Test
  void createFulfillment_ShouldMapRequestAndReturnCreated() {
    FulfillmentResource.FulfillmentRequest request =
        new FulfillmentResource.FulfillmentRequest("MWH.001", "1", "10");

    Response response = resource.createFulfillment(request);

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    ArgumentCaptor<WarehouseFulfillment> captor = ArgumentCaptor.forClass(WarehouseFulfillment.class);
    verify(createFulfillmentOperation).create(captor.capture());
    WarehouseFulfillment fulfillment = captor.getValue();
    assertEquals("MWH.001", fulfillment.warehouseBusinessUnitCode());
    assertEquals("1", fulfillment.productId());
    assertEquals("10", fulfillment.storeId());
    assertEquals(null, fulfillment.priority());
    assertEquals(null, fulfillment.createdAt());
  }

  @Test
  void removeFulfillment_ShouldDelegateAndReturnNoContent() {
    Response response = resource.removeFulfillment("MWH.001", "1", "10");

    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    verify(removeFulfillmentOperation).remove("MWH.001", "1", "10");
  }

  @Test
  void getFulfillments_WithStoreAndProduct_ShouldUseFindByProductAndStore() {
    LocalDateTime createdAt = LocalDateTime.now();
    WarehouseFulfillment fulfillment = new WarehouseFulfillment("MWH.001", "1", "10", 2, createdAt);
    when(findFulfillmentOperation.findByProductAndStore("1", "10")).thenReturn(List.of(fulfillment));

    List<FulfillmentResource.FulfillmentResponse> result = resource.getFulfillments("10", null, "1");

    assertEquals(1, result.size());
    assertEquals("MWH.001", result.get(0).warehouseBusinessUnitCode());
    assertEquals(2, result.get(0).priority());
    assertEquals(createdAt, result.get(0).createdAt());
    verify(findFulfillmentOperation).findByProductAndStore("1", "10");
  }

  @Test
  void getFulfillments_WithOnlyStore_ShouldUseFindByStore() {
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.002", "2", "20", 1, LocalDateTime.now());
    when(findFulfillmentOperation.findByStore("20")).thenReturn(List.of(fulfillment));

    List<FulfillmentResource.FulfillmentResponse> result = resource.getFulfillments("20", null, null);

    assertEquals(1, result.size());
    assertEquals("20", result.get(0).storeId());
    verify(findFulfillmentOperation).findByStore("20");
  }

  @Test
  void getFulfillments_WithOnlyWarehouse_ShouldUseFindByWarehouse() {
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment("MWH.003", "3", "30", 1, LocalDateTime.now());
    when(findFulfillmentOperation.findByWarehouse("MWH.003")).thenReturn(List.of(fulfillment));

    List<FulfillmentResource.FulfillmentResponse> result =
        resource.getFulfillments(null, "MWH.003", null);

    assertEquals(1, result.size());
    assertEquals("MWH.003", result.get(0).warehouseBusinessUnitCode());
    verify(findFulfillmentOperation).findByWarehouse("MWH.003");
  }

  @Test
  void getFulfillments_WithNoFilters_ShouldReturnEmptyList() {
    List<FulfillmentResource.FulfillmentResponse> result = resource.getFulfillments(null, null, null);

    assertTrue(result.isEmpty());
    verifyNoInteractions(findFulfillmentOperation);
  }
}
