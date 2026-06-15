package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreAlreadyExistsException;
import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StoreRepository implements StoreStore, PanacheRepository<DbStore> {

  @Override
  public List<Store> findAllStores() {
    return findAll().stream().map(DbStore::toStore).toList();
  }

  @Override
  public void create(Store store) {
    if (findByName(store.name()).isPresent()) {
      throw new StoreAlreadyExistsException(store.name());
    }

    DbStore dbStore = new DbStore(store.name(), store.quantityProductsInStock());
    persist(dbStore);
  }

  @Override
  public void update(Store store) {
    DbStore existing = findDbStoreByName(store.name());
    existing.name = store.name();
    existing.quantityProductsInStock = store.quantityProductsInStock();
    persist(existing);
  }

  @Override
  public void patch(String originalName, Store patchedStore) {
    DbStore existing = findDbStoreByName(originalName);

    if (patchedStore.name() != null) {
      existing.name = patchedStore.name();
    }

    if (patchedStore.quantityProductsInStock() != null && patchedStore.quantityProductsInStock() != 0) {
      existing.quantityProductsInStock = patchedStore.quantityProductsInStock();
    }

    persist(existing);
  }

  @Override
  public void delete(String storeName) {
    delete("name", storeName);
  }

  @Override
  public Optional<Store> findByName(String name) {
    return find("name", name).firstResultOptional().map(DbStore::toStore);
  }

  private DbStore findDbStoreByName(String storeName) {
    return find("name", storeName).firstResultOptional().orElseThrow(() -> new StoreNotFoundException(storeName));
  }


}
