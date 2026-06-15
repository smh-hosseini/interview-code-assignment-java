package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.products.exceptions.ProductException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class ProductExceptionMapper implements ExceptionMapper<ProductException> {

  @Override
  public Response toResponse(ProductException exception) {
    var status = exception.getHttpStatus();

    logException(exception, status);

    var errorResponse = new ErrorResponse(exception.getMessage(), exception.getType(), Instant.now());

    return Response.status(status)
        .entity(errorResponse)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private void logException(ProductException exception, Response.Status status) {
    int statusCode = status.getStatusCode();
    if (statusCode >= 500) {
      Log.error("Server error processing product operation", exception);
    } else if (statusCode >= 400) {
      Log.warnv("Client error: {} - {}", exception.getClass().getSimpleName(),
          exception.getMessage());
    }
  }

  public record ErrorResponse(String message,
                              String errorCode,
                              Instant timestamp) {
  }
}
