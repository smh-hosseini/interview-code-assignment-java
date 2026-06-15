package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fulfilment.application.monolith.products.exceptions.ProductException;
import com.fulfilment.application.monolith.products.exceptions.ProductValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ProductExceptionMapperTest {

  private final ProductExceptionMapper mapper = new ProductExceptionMapper();

  @Test
  void toResponse_WithValidationException_ShouldReturnBadRequestPayload() {
    Response response = mapper.toResponse(new ProductValidationException("Invalid product"));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    ProductExceptionMapper.ErrorResponse body =
        (ProductExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Invalid product", body.message());
    assertEquals("PRODUCT_VALIDATION_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  @Test
  void toResponse_WithServerException_ShouldReturnServerErrorPayload() {
    Response response = mapper.toResponse(new ProductServerException("Server failure"));

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

    ProductExceptionMapper.ErrorResponse body =
        (ProductExceptionMapper.ErrorResponse) response.getEntity();
    assertEquals("Server failure", body.message());
    assertEquals("PRODUCT_SERVER_ERROR", body.errorCode());
    assertNotNull(body.timestamp());
  }

  private static class ProductServerException extends ProductException {
    private ProductServerException(String message) {
      super(message, "PRODUCT_SERVER_ERROR");
    }

    @Override
    public Response.Status getHttpStatus() {
      return Response.Status.INTERNAL_SERVER_ERROR;
    }
  }
}
