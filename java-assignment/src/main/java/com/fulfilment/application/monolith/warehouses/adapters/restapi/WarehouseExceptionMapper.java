package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.Instant;

@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseException> {

  @Override
  public Response toResponse(WarehouseException exception) {
    var status = exception.getHttpStatus();

    logException(exception, status);

    var errorResponse = new ErrorResponse(exception.getMessage(), exception.getType(), Instant.now());

    return Response.status(status)
        .entity(errorResponse)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private void logException(WarehouseException exception, Response.Status status) {
    int statusCode = status.getStatusCode();
    if (statusCode >= 500) {
      Log.error("Server error processing warehouse operation", exception);
    } else if (statusCode >= 400) {
      Log.warnv("Client error: {0} - {1}", exception.getClass().getSimpleName(),
          exception.getMessage());
    }
  }


  public record ErrorResponse(String message,
                              String errorCode,
                              Instant timestamp) {
  }
}