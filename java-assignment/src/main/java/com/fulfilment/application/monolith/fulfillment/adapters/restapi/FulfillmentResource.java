package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.CreateFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FindFulfillmentOperation;
import com.fulfilment.application.monolith.fulfillment.domain.ports.RemoveFulfillmentOperation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/fulfillment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class FulfillmentResource {

  @Inject CreateFulfillmentOperation createFulfillmentOperation;
  @Inject RemoveFulfillmentOperation removeFulfillmentOperation;
  @Inject FindFulfillmentOperation findFulfillmentOperation;

  @POST
  public Response createFulfillment(FulfillmentRequest request) {
    WarehouseFulfillment fulfillment =
        new WarehouseFulfillment(
            request.warehouseBusinessUnitCode(),
            request.productId(),
            request.storeId(),
            null,
            null);
    createFulfillmentOperation.create(fulfillment);
    return Response.status(Response.Status.CREATED).build();
  }

  @DELETE
  @Path("/{warehouseCode}/{productId}/{storeId}")
  public Response removeFulfillment(
      @PathParam("warehouseCode") String warehouseCode,
      @PathParam("productId") String productId,
      @PathParam("storeId") String storeId) {
    removeFulfillmentOperation.remove(warehouseCode, productId, storeId);
    return Response.noContent().build();
  }

  @GET
  public List<FulfillmentResponse> getFulfillments(
      @QueryParam("storeId") String storeId,
      @QueryParam("warehouseCode") String warehouseCode,
      @QueryParam("productId") String productId) {

    List<WarehouseFulfillment> fulfillments;

    if (storeId != null && productId != null) {
      fulfillments = findFulfillmentOperation.findByProductAndStore(productId, storeId);
    } else if (storeId != null) {
      fulfillments = findFulfillmentOperation.findByStore(storeId);
    } else if (warehouseCode != null) {
      fulfillments = findFulfillmentOperation.findByWarehouse(warehouseCode);
    } else {
      fulfillments = List.of();
    }

    return fulfillments.stream().map(this::toFulfillmentResponse).toList();
  }

  private FulfillmentResponse toFulfillmentResponse(WarehouseFulfillment fulfillment) {
    return new FulfillmentResponse(
        fulfillment.warehouseBusinessUnitCode(),
        fulfillment.productId(),
        fulfillment.storeId(),
        fulfillment.priority(),
        fulfillment.createdAt());
  }

  public record FulfillmentRequest(
      String warehouseBusinessUnitCode, String productId, String storeId) {}

  public record FulfillmentResponse(
      String warehouseBusinessUnitCode,
      String productId,
      String storeId,
      Integer priority,
      java.time.LocalDateTime createdAt) {}
}
