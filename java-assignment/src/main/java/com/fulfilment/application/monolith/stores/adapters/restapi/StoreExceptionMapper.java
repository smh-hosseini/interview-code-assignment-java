package com.fulfilment.application.monolith.stores.adapters.restapi;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class StoreExceptionMapper implements ExceptionMapper<StoreException> {

  @Override
  public Response toResponse(StoreException exception) {
    var status = exception.getHttpStatus();

    logException(exception, status);

    var errorResponse = new ErrorResponse(exception.getMessage(), exception.getType(), Instant.now());

    return Response.status(status)
        .entity(errorResponse)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private void logException(StoreException exception, Response.Status status) {
    int statusCode = status.getStatusCode();
    if (statusCode >= 500) {
      Log.error("Server error processing store operation", exception);
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