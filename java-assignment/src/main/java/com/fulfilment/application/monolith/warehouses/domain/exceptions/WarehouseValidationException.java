package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class WarehouseValidationException extends WarehouseException {

  public WarehouseValidationException(String message) {
    super(message, "WAREHOUSE_VALIDATION_EXCEPTION");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.BAD_REQUEST;
  }
}