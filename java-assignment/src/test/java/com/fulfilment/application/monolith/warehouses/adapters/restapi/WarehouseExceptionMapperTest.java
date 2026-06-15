package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class WarehouseExceptionMapperTest {

  private final WarehouseExceptionMapper mapper = new WarehouseExceptionMapper();

  @Test
  void toResponse_WithValidationException_ShouldReturnBadRequestPayload() {
    Response response = mapper.toResponse(new WarehouseValidationException("Invalid warehouse"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    WarehouseExceptionMapper.ErrorResponse body =
        (WarehouseExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Invalid warehouse", body.message());
    assertEquals("WAREHOUSE_VALIDATION_EXCEPTION", body.errorCode());
    assertNotNull(body.timestamp());
  }

  @Test
  void toResponse_WithServerException_ShouldReturnServerErrorPayload() {
    Response response = mapper.toResponse(new WarehouseServerException("Server failure"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    WarehouseExceptionMapper.ErrorResponse body =
        (WarehouseExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Server failure", body.message());
    assertEquals("WAREHOUSE_SERVER_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  private static class WarehouseServerException extends WarehouseException {
    private WarehouseServerException(String message) {
      super(message, "WAREHOUSE_SERVER_ERROR");
    }

    @Override
    public Response.Status getHttpStatus() {
      return Response.Status.INTERNAL_SERVER_ERROR;
    }
  }
}
