package com.fulfilment.application.monolith.products.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class ProductException extends RuntimeException {

  private final String type;

  protected ProductException(String message, String type) {
    super(message);
    this.type = type;
  }

  public abstract Response.Status getHttpStatus();

  public String getType() {
    return type;
  }
}
