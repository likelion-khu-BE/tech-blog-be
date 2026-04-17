package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "generation",
    indexes = {
      @Index(name = "idx_generation_number", columnList = "number", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false, unique = true)
  private Integer number;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "is_current", nullable = false)
  private Boolean isCurrent = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static Generation create(String label, Integer number, LocalDate startDate) {
    Generation generation = new Generation();
    generation.label = label;
    generation.number = number;
    generation.startDate = startDate;
    generation.isCurrent = false;
    return generation;
  }

  public void markAsCurrent() {
    this.isCurrent = true;
  }

  public void close(LocalDate endDate) {
    this.endDate = endDate;
    this.isCurrent = false;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
