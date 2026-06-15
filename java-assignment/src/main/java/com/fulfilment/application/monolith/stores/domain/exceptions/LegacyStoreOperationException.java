package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class LegacyStoreOperationException extends StoreException {

  public LegacyStoreOperationException(String storeName, Throwable cause) {
    super(
        "Failed to sync store '" + storeName + "' with legacy system.",
        "LEGACY_STORE_OPERATION_ERROR",
        cause);
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.INTERNAL_SERVER_ERROR;
  }
}
