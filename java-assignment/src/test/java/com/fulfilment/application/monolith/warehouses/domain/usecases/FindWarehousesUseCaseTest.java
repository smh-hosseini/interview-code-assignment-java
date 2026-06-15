package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindWarehousesUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private FindWarehousesUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new FindWarehousesUseCase(warehouseStore);
  }

  @Test
  void findAllActive_ShouldReturnOnlyNonArchivedWarehouses() {
    Warehouse active = new Warehouse("MWH.001", "AMSTERDAM-001", 100, 20, LocalDateTime.now(), null);
    Warehouse archived =
        new Warehouse(
            "MWH.002",
            "AMSTERDAM-002",
            100,
            20,
            LocalDateTime.now(),
            LocalDateTime.now().minusDays(1));

    when(warehouseStore.getAll()).thenReturn(List.of(active, archived));

    List<Warehouse> result = useCase.findAllActive();

    assertEquals(1, result.size());
    assertEquals(active, result.get(0));
  }

  @Test
  void findByBusinessUnitCode_WhenWarehouseExists_ShouldReturnWarehouse() {
    Warehouse warehouse =
        new Warehouse("MWH.001", "AMSTERDAM-001", 100, 20, LocalDateTime.now(), null);
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(Optional.of(warehouse));

    Warehouse result = useCase.findByBusinessUnitCode("MWH.001");

    assertEquals(warehouse, result);
  }

  @Test
  void findByBusinessUnitCode_WhenWarehouseMissing_ShouldThrowNotFound() {
    when(warehouseStore.findByBusinessUnitCode("MWH.404")).thenReturn(Optional.empty());

    assertThrows(WarehouseNotFoundException.class, () -> useCase.findByBusinessUnitCode("MWH.404"));
  }
}
