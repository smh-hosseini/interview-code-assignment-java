package com.fulfilment.application.monolith.warehouses.domain.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class WarehouseException extends RuntimeException {

  private final String type;

  protected WarehouseException(String message, String type) {
    super(message);
    this.type = type;
  }

  public abstract Response.Status getHttpStatus();

  public String getType() {
    return type;
  }

}