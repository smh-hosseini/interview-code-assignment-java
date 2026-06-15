package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import jakarta.persistence.*;

@Entity
@Table(name = "store")
@Cacheable
public class DbStore {

  @Id
  @GeneratedValue
  public Long id;

  @Column(length = 40, unique = true)
  public String name;

  public int quantityProductsInStock;

  public DbStore() {
    // Default constructor for JPA
  }

  public DbStore(String name) {
    this.name = name;
    this.quantityProductsInStock = 0;
  }

  public DbStore(String name, int quantityProductsInStock) {
    this.name = name;
    this.quantityProductsInStock = quantityProductsInStock;
  }

  public Store toStore() {
    return new Store(this.name, this.quantityProductsInStock);
  }
}
