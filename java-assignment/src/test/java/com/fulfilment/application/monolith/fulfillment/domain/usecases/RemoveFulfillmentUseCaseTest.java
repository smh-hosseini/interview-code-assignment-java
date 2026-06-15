package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoveFulfillmentUseCaseTest {

  @Mock private FulfillmentStore fulfillmentStore;

  private RemoveFulfillmentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RemoveFulfillmentUseCase(fulfillmentStore);
  }

  @Test
  void remove_WhenAssociationExists_ShouldRemove() {
    when(fulfillmentStore.exists("MWH.001", "1", "10")).thenReturn(true);

    useCase.remove("MWH.001", "1", "10");

    verify(fulfillmentStore).remove("MWH.001", "1", "10");
  }

  @Test
  void remove_WhenAssociationDoesNotExist_ShouldThrowNotFound() {
    when(fulfillmentStore.exists("MWH.001", "1", "10")).thenReturn(false);

    assertThrows(
        FulfillmentNotFoundException.class, () -> useCase.remove("MWH.001", "1", "10"));

    verify(fulfillmentStore, never()).remove("MWH.001", "1", "10");
  }
}
