package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import jakarta.ws.rs.core.Response;

public class WarehouseNotFoundException extends WarehouseException {

  public WarehouseNotFoundException(String businessUnitCode) {
    super("Warehouse with business unit code '" + businessUnitCode + "' not found", "WAREHOUSE_NOT_FOUND");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.NOT_FOUND;
  }
}