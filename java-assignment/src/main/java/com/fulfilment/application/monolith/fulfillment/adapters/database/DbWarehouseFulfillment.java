package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "warehouse_fulfillment",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_warehouse_product_store",
          columnNames = {"warehouse_id", "product_id", "store_id"})
    },
    indexes = {
      @Index(name = "idx_warehouse", columnList = "warehouse_id"),
      @Index(name = "idx_product_store", columnList = "product_id,store_id"),
      @Index(name = "idx_store", columnList = "store_id")
    })
@Cacheable
public class DbWarehouseFulfillment {

  @Id @GeneratedValue public Long id;

  @Column(name = "warehouse_id", nullable = false)
  public String warehouseBusinessUnitCode;

  @Column(name = "product_id", nullable = false)
  public Long productId;

  @Column(name = "store_id", nullable = false)
  public Long storeId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  public LocalDateTime createdAt;

  @Column(name = "priority")
  public Integer priority;

  public DbWarehouseFulfillment() {}

  public WarehouseFulfillment toWarehouseFulfillment() {
    return new WarehouseFulfillment(
        this.warehouseBusinessUnitCode,
        String.valueOf(this.productId),
        String.valueOf(this.storeId),
        this.priority,
        this.createdAt);
  }
}
