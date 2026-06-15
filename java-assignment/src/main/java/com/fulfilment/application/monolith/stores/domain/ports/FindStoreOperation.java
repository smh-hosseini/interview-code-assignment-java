package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

import java.util.List;

public interface FindStoreOperation {

  List<Store> findAll();
  Store findByName(String storeName);

}
