package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Cacheable
public class DbWarehouse {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false)
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  @CreationTimestamp
  @Column(updatable = false)
  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public DbWarehouse() {}

  public Warehouse toWarehouse() {
    return new Warehouse(this.businessUnitCode,
        this.location,
        this.capacity,
        this.stock,
        this.createdAt,
        this.archivedAt);
  }
}
