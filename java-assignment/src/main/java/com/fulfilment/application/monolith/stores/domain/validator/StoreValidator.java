package com.fulfilment.application.monolith.stores.domain.validator;

import com.fulfilment.application.monolith.stores.domain.exceptions.StoreValidationException;
import com.fulfilment.application.monolith.stores.domain.models.Store;

public class StoreValidator {


  public static void validate(Store store) {
    validateStoreName(store.name());
    validateQuantity(store.quantityProductsInStock());
  }

  private static void validateQuantity(int quantity) {
    if (quantity < 0) {
      throw new StoreValidationException("Quantity in stock cannot be negative");
    }
  }

  private static void validateStoreName(String name) {
    if (name == null || name.isBlank()) {
      throw new StoreValidationException("Store name is required");
    }

    if (name.length() > 40) {
      throw new StoreValidationException("Store name cannot exceed 40 characters");
    }
  }
}
