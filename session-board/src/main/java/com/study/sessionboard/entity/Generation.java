package com.study.sessionboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "generation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String label;

  @Column(name = "is_current", nullable = false)
  private boolean isCurrent;

  public static Generation of(String label) {
    Generation gen = new Generation();
    gen.label = label;
    return gen;
  }
}
