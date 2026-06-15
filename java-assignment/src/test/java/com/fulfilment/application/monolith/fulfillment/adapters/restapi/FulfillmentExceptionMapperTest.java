package com.fulfilment.application.monolith.fulfillment.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class FulfillmentExceptionMapperTest {

  private final FulfillmentExceptionMapper mapper = new FulfillmentExceptionMapper();

  @Test
  void toResponse_WithValidationException_ShouldReturnBadRequestPayload() {
    Response response = mapper.toResponse(new FulfillmentValidationException("Invalid fulfillment"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    FulfillmentExceptionMapper.ErrorResponse body =
        (FulfillmentExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Invalid fulfillment", body.message());
    assertEquals("FULFILLMENT_VALIDATION_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  @Test
  void toResponse_WithServerException_ShouldReturnServerErrorPayload() {
    Response response = mapper.toResponse(new FulfillmentServerException("Server failure"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    FulfillmentExceptionMapper.ErrorResponse body =
        (FulfillmentExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Server failure", body.message());
    assertEquals("FULFILLMENT_SERVER_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  private static class FulfillmentServerException extends FulfillmentException {
    private FulfillmentServerException(String message) {
      super(message, "FULFILLMENT_SERVER_ERROR");
    }

    @Override
    public Response.Status getHttpStatus() {
      return Response.Status.INTERNAL_SERVER_ERROR;
    }
  }
}
