package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class StoreValidationException extends StoreException {

  public StoreValidationException(String message) {
    super(message, "STORE_VALIDATION_ERROR");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.BAD_REQUEST;
  }
}