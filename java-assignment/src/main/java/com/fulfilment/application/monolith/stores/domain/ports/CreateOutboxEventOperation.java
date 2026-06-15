package com.fulfilment.application.monolith.stores.domain.ports;


import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;

public interface CreateOutboxEventOperation {
  void create(OutboxEvent event);
}
