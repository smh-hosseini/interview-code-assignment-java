package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.FindWarehousesOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FindWarehousesUseCase implements FindWarehousesOperation {

    private final WarehouseStore warehouseStore;

    public FindWarehousesUseCase(WarehouseStore warehouseStore) {
        this.warehouseStore = warehouseStore;
    }

    @Transactional
    @Override
    public List<Warehouse> findAllActive() {
        return warehouseStore.getAll().stream()
                .filter(wh -> wh.archivedAt() == null) // Only return active warehouses
                .toList();
    }

    @Transactional
    @Override
    public Warehouse findByBusinessUnitCode(String businessUnitCode) {
        return warehouseStore.findByBusinessUnitCode(businessUnitCode)
            .orElseThrow(() -> new WarehouseNotFoundException(businessUnitCode));
    }

}
