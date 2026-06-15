package com.fulfilment.application.monolith.stores.adapters.restapi;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import javax.validation.Valid;
import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject FindStoreOperation findStoreOperation;
  @Inject CreateStoreOperation createStoreOperation;
  @Inject PatchStoreOperation patchStoreOperation;
  @Inject RemoveStoreOperation removeStoreOperation;
  @Inject UpdateStoreOperation updateStoreOperation;

  @GET
  public List<Store> get() {
    return findStoreOperation.findAll();
  }

  @GET
  @Path("{store-name}")
  public Store getSingle(@PathParam("store-name") String storeName) {
    return findStoreOperation.findByName(storeName);
  }

  @POST
  public Response create(@Valid Store store) {
    createStoreOperation.create(store);
    return Response.ok().status(201).build();
  }

  @PUT
  @Path("{store-name}")
  public void update(@PathParam("store-name") String storeName, @Valid Store store) {
    updateStoreOperation.update(storeName, store);
  }

  @PATCH
  @Path("{store-name}")
  public void patch(@PathParam("store-name") String storeName, @Valid Store store) {
    patchStoreOperation.patch(storeName, store);
  }

  @DELETE
  @Path("{store-name}")
  public Response delete(@PathParam("store-name") String storeName) {
    removeStoreOperation.remove(storeName);
    return Response.status(204).build();
  }
}
