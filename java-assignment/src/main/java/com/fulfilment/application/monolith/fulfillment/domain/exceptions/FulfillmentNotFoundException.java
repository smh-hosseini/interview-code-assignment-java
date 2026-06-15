package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class FulfillmentNotFoundException extends FulfillmentException {

  public FulfillmentNotFoundException(String warehouse, String product, String store) {
    super(
        "Fulfillment association not found: Warehouse '"
            + warehouse
            + "' -> Product '"
            + product
            + "' -> Store '"
            + store
            + "'",
        "FULFILLMENT_NOT_FOUND");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.NOT_FOUND;
  }
}
