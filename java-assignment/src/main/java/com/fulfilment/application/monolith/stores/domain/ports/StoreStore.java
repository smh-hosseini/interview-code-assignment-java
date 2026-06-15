package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import java.util.List;
import java.util.Optional;

public interface StoreStore {

  List<Store> findAllStores();

  void create(Store store);

  void update(Store store);

  void patch(String originalName, Store patchedStore);

  void delete(String storeName);

  Optional<Store> findByName(String name);
}
