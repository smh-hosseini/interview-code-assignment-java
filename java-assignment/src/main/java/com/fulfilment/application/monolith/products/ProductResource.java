package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.products.exceptions.ProductNotFoundException;
import com.fulfilment.application.monolith.products.exceptions.ProductValidationException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  @GET
  public List<Product> get() {
    return productRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new ProductNotFoundException(id);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    if (product.getId() != null) {
      throw new ProductValidationException("Id was invalidly set on request.");
    }

    productRepository.persist(product);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    if (product.getName() == null) {
      throw new ProductValidationException("Product Name was not set on request.");
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      throw new ProductNotFoundException(id);
    }

    entity.setName(product.getName());
    entity.setDescription(product.getDescription());
    entity.setPrice(product.getPrice());
    entity.setStock(product.getStock());

    productRepository.persist(entity);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new ProductNotFoundException(id);
    }
    productRepository.delete(entity);
    return Response.status(204).build();
  }
}
