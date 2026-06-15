package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class StoreNotFoundException extends StoreException {

  public StoreNotFoundException(String storeName) {
    super("Store with name of " + storeName + " does not exist.", "STORE_NOT_FOUND");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.NOT_FOUND;
  }
}