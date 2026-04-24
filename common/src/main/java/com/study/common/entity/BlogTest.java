package com.study.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blog_test")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogTest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
