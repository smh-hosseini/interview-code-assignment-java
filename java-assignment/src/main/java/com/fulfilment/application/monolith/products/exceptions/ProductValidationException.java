package com.fulfilment.application.monolith.products.exceptions;

import jakarta.ws.rs.core.Response;

public class ProductValidationException extends ProductException {

  public ProductValidationException(String message) {
    super(message, "PRODUCT_VALIDATION_ERROR");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.BAD_REQUEST;
  }
}
