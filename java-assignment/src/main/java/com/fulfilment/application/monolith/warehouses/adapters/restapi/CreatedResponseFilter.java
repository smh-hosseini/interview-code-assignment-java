package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;


//workaround for 201 issue
@Provider
public class CreatedResponseFilter implements ContainerResponseFilter {
  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    if ("POST".equalsIgnoreCase(req.getMethod())
        && res.getStatus() == 200
        && res.getEntity() instanceof Warehouse) {
      res.setStatus(Response.Status.CREATED.getStatusCode());
    }
  }
}