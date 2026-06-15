package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.FindWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject FindWarehousesOperation findWarehousesOperation;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return findWarehousesOperation.findAllActive().stream()
        .map(this::toWarehouseResponse)
        .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = toWarehouseDomain(data);
    createWarehouseOperation.create(warehouse);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByBusinessUnitCode(String businessUnitCode) {
    var warehouse = findWarehousesOperation.findByBusinessUnitCode(businessUnitCode);
    return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByBusinessUnitCode(String businessUnitCode) {
    var warehouse = findWarehousesOperation.findByBusinessUnitCode(businessUnitCode);
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    data.setBusinessUnitCode(businessUnitCode);
    var warehouse = toWarehouseDomain(data);
    replaceWarehouseOperation.replace(warehouse);
    return toWarehouseResponse(warehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.businessUnitCode()); // Use businessUnitCode as the ID
    response.setBusinessUnitCode(warehouse.businessUnitCode());
    response.setLocation(warehouse.location());
    response.setCapacity(warehouse.capacity());
    response.setStock(warehouse.stock());
    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toWarehouseDomain(
      Warehouse data) {
    return new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse(data.getBusinessUnitCode(),
            data.getLocation(),
            data.getCapacity(),
            data.getStock(),
            null,
            null);
  }

}

