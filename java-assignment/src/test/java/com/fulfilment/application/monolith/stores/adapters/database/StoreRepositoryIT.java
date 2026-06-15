package com.fulfilment.application.monolith.stores.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreAlreadyExistsException;
import com.fulfilment.application.monolith.stores.domain.exceptions.StoreNotFoundException;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreRepositoryIT {

  @Inject StoreRepository storeRepository;

  @Test
  @TestTransaction
  void createShouldPersistStore() {
    storeRepository.create(new Store("STORE_REPO_CREATE", 12));

    Optional<Store> created = storeRepository.findByName("STORE_REPO_CREATE");

    assertTrue(created.isPresent());
    assertEquals("STORE_REPO_CREATE", created.get().name());
    assertEquals(12, created.get().quantityProductsInStock());
  }

  @Test
  @TestTransaction
  void createShouldFailForDuplicateName() {
    assertThrows(
        StoreAlreadyExistsException.class,
        () -> storeRepository.create(new Store("TONSTAD", 1)));
  }

  @Test
  @TestTransaction
  void updateShouldPersistChanges() {
    storeRepository.create(new Store("STORE_REPO_UPDATE", 7));
    storeRepository.update(new Store("STORE_REPO_UPDATE", 18));

    Optional<Store> updated = storeRepository.findByName("STORE_REPO_UPDATE");
    assertTrue(updated.isPresent());
    assertEquals(18, updated.get().quantityProductsInStock());
  }

  @Test
  @TestTransaction
  void updateShouldFailWhenStoreDoesNotExist() {
    assertThrows(
        StoreNotFoundException.class,
        () -> storeRepository.update(new Store("STORE_REPO_MISSING", 5)));
  }

  @Test
  @TestTransaction
  void patchShouldUpdateQuantityOnly() {
    storeRepository.create(new Store("STORE_REPO_PATCH", 9));
    storeRepository.patch("STORE_REPO_PATCH", new Store(null, 20));

    Optional<Store> patched = storeRepository.findByName("STORE_REPO_PATCH");
    assertTrue(patched.isPresent());
    assertEquals("STORE_REPO_PATCH", patched.get().name());
    assertEquals(20, patched.get().quantityProductsInStock());
  }

  @Test
  @TestTransaction
  void patchShouldRenameStoreAndKeepQuantityWhenQuantityMissing() {
    storeRepository.create(new Store("STORE_REPO_RENAME", 15));
    storeRepository.patch("STORE_REPO_RENAME", new Store("STORE_REPO_RENAMED", null));

    assertTrue(storeRepository.findByName("STORE_REPO_RENAME").isEmpty());
    Optional<Store> renamed = storeRepository.findByName("STORE_REPO_RENAMED");
    assertTrue(renamed.isPresent());
    assertEquals(15, renamed.get().quantityProductsInStock());
  }

  @Test
  @TestTransaction
  void patchShouldFailWhenStoreDoesNotExist() {
    assertThrows(
        StoreNotFoundException.class,
        () -> storeRepository.patch("STORE_REPO_MISSING", new Store("ANY", 1)));
  }

  @Test
  @TestTransaction
  void deleteShouldRemoveStore() {
    storeRepository.create(new Store("STORE_REPO_DELETE", 5));
    assertTrue(storeRepository.findByName("STORE_REPO_DELETE").isPresent());

    storeRepository.delete("STORE_REPO_DELETE");

    assertTrue(storeRepository.findByName("STORE_REPO_DELETE").isEmpty());
  }

  @Test
  @TestTransaction
  void findAllStoresShouldReturnSeededAndInsertedStores() {
    storeRepository.create(new Store("STORE_REPO_LIST", 3));

    var stores = storeRepository.findAllStores();

    assertFalse(stores.isEmpty());
    assertTrue(stores.stream().anyMatch(store -> "TONSTAD".equals(store.name())));
    assertTrue(stores.stream().anyMatch(store -> "STORE_REPO_LIST".equals(store.name())));
  }
}
