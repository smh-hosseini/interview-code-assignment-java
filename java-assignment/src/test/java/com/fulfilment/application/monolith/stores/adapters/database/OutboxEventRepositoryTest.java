package com.fulfilment.application.monolith.stores.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.stores.domain.exceptions.OutboxSerializationException;
import com.fulfilment.application.monolith.stores.domain.models.EventType;
import com.fulfilment.application.monolith.stores.domain.models.OutboxEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import org.junit.jupiter.api.Test;

class OutboxEventRepositoryTest {

  @Test
  void create_WhenPayloadSerializationFails_ShouldThrowDomainException() throws Exception {
    OutboxEventRepository repository = new OutboxEventRepository();
    ObjectMapper objectMapper = mock(ObjectMapper.class);
    repository.objectMapper = objectMapper;

    OutboxEvent event = new OutboxEvent(new Store("SERIALIZATION_FAIL", 3), EventType.STORE_CREATED, null);

    when(objectMapper.writeValueAsString(event.store()))
        .thenThrow(new JsonProcessingException("boom") {});

    OutboxSerializationException exception =
        assertThrows(OutboxSerializationException.class, () -> repository.create(event));

    assertEquals("OUTBOX_SERIALIZATION_ERROR", exception.getType());
  }
}
