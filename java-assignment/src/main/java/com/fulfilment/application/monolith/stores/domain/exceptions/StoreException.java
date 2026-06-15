package com.fulfilment.application.monolith.stores.domain.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class StoreException extends RuntimeException {

  private final String type;

  protected StoreException(String message, String type) {
    super(message);
    this.type = type;
  }

  protected StoreException(String message, String type, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public abstract Response.Status getHttpStatus();

  public String getType() {
    return type;
  }
}