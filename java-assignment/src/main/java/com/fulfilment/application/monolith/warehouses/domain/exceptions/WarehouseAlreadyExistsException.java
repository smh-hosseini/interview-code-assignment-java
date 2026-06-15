package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class WarehouseAlreadyExistsException extends WarehouseException {

  public WarehouseAlreadyExistsException(String businessUnitCode) {
    super("Warehouse with business unit code '" + businessUnitCode + "' already exists", "WAREHOUSE_ALREADY_EXISTS");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.CONFLICT;
  }
}