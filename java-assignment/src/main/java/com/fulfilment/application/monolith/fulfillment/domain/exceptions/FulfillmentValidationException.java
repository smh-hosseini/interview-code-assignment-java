package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class FulfillmentValidationException extends FulfillmentException {

  public FulfillmentValidationException(String message) {
    super(message, "FULFILLMENT_VALIDATION_ERROR");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.BAD_REQUEST;
  }
}
