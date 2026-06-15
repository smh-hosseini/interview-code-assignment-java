package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class FulfillmentAlreadyExistsException extends FulfillmentException {

  public FulfillmentAlreadyExistsException(String warehouse, String product, String store) {
    super(
        "Fulfillment association already exists: Warehouse '"
            + warehouse
            + "' -> Product '"
            + product
            + "' -> Store '"
            + store
            + "'",
        "FULFILLMENT_ALREADY_EXISTS");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.CONFLICT;
  }
}
