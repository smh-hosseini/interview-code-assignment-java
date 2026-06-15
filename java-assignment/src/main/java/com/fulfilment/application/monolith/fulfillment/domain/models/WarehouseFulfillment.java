package com.fulfilment.application.monolith.fulfillment.domain.models;

import java.time.LocalDateTime;

public record WarehouseFulfillment(
    String warehouseBusinessUnitCode,
    String productId,
    String storeId,
    Integer priority,
    LocalDateTime createdAt
) {
}
