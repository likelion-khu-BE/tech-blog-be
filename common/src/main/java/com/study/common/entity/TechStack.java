package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tech_stack",
    indexes = {
      @Index(name = "idx_tech_stack_name", columnList = "name", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TechStackCategory category;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static TechStack create(String name, TechStackCategory category) {
    TechStack techStack = new TechStack();
    techStack.name = name;
    techStack.category = category;
    return techStack;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
