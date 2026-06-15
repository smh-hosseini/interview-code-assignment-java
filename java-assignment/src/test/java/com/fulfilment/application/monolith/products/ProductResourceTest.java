package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.products.exceptions.ProductNotFoundException;
import com.fulfilment.application.monolith.products.exceptions.ProductValidationException;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductResourceTest {

  @Mock private ProductRepository productRepository;

  private ProductResource resource;

  @BeforeEach
  void setUp() {
    resource = new ProductResource();
    resource.productRepository = productRepository;
  }

  @Test
  void get_ShouldReturnSortedProducts() {
    Product first = new Product("ALPHA");
    Product second = new Product("BETA");
    when(productRepository.listAll(any(Sort.class))).thenReturn(List.of(first, second));

    List<Product> result = resource.get();

    assertEquals(2, result.size());
    assertEquals("ALPHA", result.get(0).getName());
    verify(productRepository).listAll(any(Sort.class));
  }

  @Test
  void getSingle_WhenMissing_ShouldThrowNotFound() {
    when(productRepository.findById(42L)).thenReturn(null);

    assertThrows(ProductNotFoundException.class, () -> resource.getSingle(42L));
  }

  @Test
  void create_WhenIdProvided_ShouldThrowValidationError() {
    Product product = new Product("KALLAX");
    product.setId(1L);

    assertThrows(ProductValidationException.class, () -> resource.create(product));
    verify(productRepository, never()).persist(any(Product.class));
  }

  @Test
  void create_WhenValid_ShouldPersistAndReturnCreatedResponse() {
    Product product = new Product("KALLAX");

    Response response = resource.create(product);

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    assertSame(product, response.getEntity());
    verify(productRepository).persist(product);
  }

  @Test
  void update_WhenNameMissing_ShouldThrowValidationError() {
    Product input = new Product();
    input.setName(null);

    assertThrows(ProductValidationException.class, () -> resource.update(10L, input));
    verify(productRepository, never()).findById(any(Long.class));
  }

  @Test
  void update_WhenProductMissing_ShouldThrowNotFound() {
    Product input = new Product("MALM");
    when(productRepository.findById(10L)).thenReturn(null);

    assertThrows(ProductNotFoundException.class, () -> resource.update(10L, input));
  }

  @Test
  void update_WhenValid_ShouldUpdateAndPersistExistingEntity() {
    Product existing = new Product("MALM");
    existing.setDescription("Old");
    existing.setPrice(new BigDecimal("1.00"));
    existing.setStock(2);

    Product input = new Product("MALM-UPDATED");
    input.setDescription("Updated");
    input.setPrice(new BigDecimal("12.34"));
    input.setStock(9);

    when(productRepository.findById(20L)).thenReturn(existing);

    Product result = resource.update(20L, input);

    assertSame(existing, result);
    assertEquals("MALM-UPDATED", result.getName());
    assertEquals("Updated", result.getDescription());
    assertEquals(new BigDecimal("12.34"), result.getPrice());
    assertEquals(9, result.getStock());
    verify(productRepository).persist(existing);
  }

  @Test
  void delete_WhenProductMissing_ShouldThrowNotFound() {
    when(productRepository.findById(50L)).thenReturn(null);

    assertThrows(ProductNotFoundException.class, () -> resource.delete(50L));
  }

  @Test
  void delete_WhenProductExists_ShouldDeleteAndReturnNoContent() {
    Product existing = new Product("POANG");
    when(productRepository.findById(50L)).thenReturn(existing);

    Response response = resource.delete(50L);

    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    verify(productRepository).delete(existing);
  }
}
