package com.fulfilment.application.monolith.stores.domain.models;

public record OutboxEvent(Store store, EventType eventType, Long eventId) {
}
