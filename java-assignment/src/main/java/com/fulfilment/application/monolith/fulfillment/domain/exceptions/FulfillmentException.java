package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class FulfillmentException extends RuntimeException {
  private final String type;

  protected FulfillmentException(String message, String type) {
    super(message);
    this.type = type;
  }

  public abstract Response.Status getHttpStatus();

  public String getType() {
    return type;
  }
}
