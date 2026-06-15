package com.fulfilment.application.monolith.products.exceptions;

import jakarta.ws.rs.core.Response;

public class ProductNotFoundException extends ProductException {

  public ProductNotFoundException(Long productId) {
    super("Product with id of " + productId + " does not exist.", "PRODUCT_NOT_FOUND");
  }

  @Override
  public Response.Status getHttpStatus() {
    return Response.Status.NOT_FOUND;
  }
}
