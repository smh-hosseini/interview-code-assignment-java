package com.fulfilment.application.monolith.stores.domain.models;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public record Store(@NotEmpty @Size(max = 40)  String name, @Min(value = 0) Integer quantityProductsInStock) {
}
