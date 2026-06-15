package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;

import java.util.List;

public interface OutboxEventStore {

  void create(OutboxEvent event);
  void processed(OutboxEvent event);
  List<OutboxEvent> findPendingEvents();
}
