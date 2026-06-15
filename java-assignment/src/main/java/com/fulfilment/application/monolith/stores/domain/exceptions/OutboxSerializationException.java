package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class OutboxSerializationException extends StoreException {

  public OutboxSerializationException(String storeName, Throwable cause) {
    super(
        "Failed to serialize outbox event payload for store '" + storeName + "'.",
        "OUTBOX_SERIALIZATION_ERROR",
        cause);
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.INTERNAL_SERVER_ERROR;
  }
}
