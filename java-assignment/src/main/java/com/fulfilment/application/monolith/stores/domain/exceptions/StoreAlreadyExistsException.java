package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class StoreAlreadyExistsException extends StoreException {

  public StoreAlreadyExistsException(String storeName) {
    super("Store with name  " + storeName + " already exists.", "STORE_ALREADY_EXISTS");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.BAD_REQUEST;
  }
}