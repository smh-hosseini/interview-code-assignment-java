package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.Instant;

@Provider
public class FulfillmentExceptionMapper implements ExceptionMapper<FulfillmentException> {

  private static final Logger LOG = Logger.getLogger(FulfillmentExceptionMapper.class);

  @Override
  public Response toResponse(FulfillmentException exception) {
    var status = exception.getHttpStatus();
    logException(exception, status);

    var errorResponse =
        new ErrorResponse(exception.getMessage(), exception.getType(), Instant.now());

    return Response.status(status).entity(errorResponse).type(MediaType.APPLICATION_JSON).build();
  }

  private void logException(FulfillmentException exception, Response.Status status) {
    if (status.getStatusCode() >= 500) {
      LOG.error("Fulfillment error: " + exception.getMessage(), exception);
    } else {
      LOG.warn("Fulfillment validation error: " + exception.getMessage());
    }
  }

  public record ErrorResponse(String message, String errorCode, Instant timestamp) {}
}
