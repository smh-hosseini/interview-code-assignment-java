package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.domain.models.EventType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class DbOutboxEvent {

  @Id
  @GeneratedValue
  public Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EventType eventType;

  @Column(nullable = false, length = 1000)
  public String payload;

  @Column(nullable = false)
  @CreationTimestamp
  public LocalDateTime createdAt;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public ProcessingStatus status;

  public LocalDateTime processedAt;

  public DbOutboxEvent() {
    // Default constructor for JPA
  }

  public DbOutboxEvent(EventType eventType, String payload) {
    this.eventType = eventType;
    this.payload = payload;
    this.createdAt = LocalDateTime.now();
    this.status = ProcessingStatus.PENDING;
  }

  public enum ProcessingStatus {
    PENDING,
    PROCESSED,
    FAILED
  }

}