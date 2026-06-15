package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public record Warehouse (String businessUnitCode, String location, Integer capacity, Integer stock,
                         LocalDateTime createdAt, LocalDateTime archivedAt) {

}
