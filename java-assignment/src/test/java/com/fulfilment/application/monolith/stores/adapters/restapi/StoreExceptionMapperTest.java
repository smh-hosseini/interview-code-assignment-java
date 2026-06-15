package com.fulfilment.application.monolith.stores.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreException;
import com.fulfilment.application.monolith.stores.domain.exceptions.StoreValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class StoreExceptionMapperTest {

  private final StoreExceptionMapper mapper = new StoreExceptionMapper();

  @Test
  void toResponse_WithValidationException_ShouldReturnBadRequestPayload() {
    Response response = mapper.toResponse(new StoreValidationException("Invalid store"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    StoreExceptionMapper.ErrorResponse body = (StoreExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Invalid store", body.message());
    assertEquals("STORE_VALIDATION_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  @Test
  void toResponse_WithServerException_ShouldReturnServerErrorPayload() {
    Response response = mapper.toResponse(new StoreServerException("Server failure"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    StoreExceptionMapper.ErrorResponse body = (StoreExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Server failure", body.message());
    assertEquals("STORE_SERVER_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  private static class StoreServerException extends StoreException {
    private StoreServerException(String message) {
      super(message, "STORE_SERVER_ERROR");
    }

    @Override
    public Response.Status getHttpStatus() {
      return Response.Status.INTERNAL_SERVER_ERROR;
    }
  }
}
