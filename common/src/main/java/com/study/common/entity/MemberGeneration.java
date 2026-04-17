package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_generation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGeneration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "generation_id")
  private Generation generation;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_in_gen", nullable = false)
  private GenerationRole roleInGen = GenerationRole.member;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  public static MemberGeneration create(Member member, Generation generation, GenerationRole role) {
    MemberGeneration mg = new MemberGeneration();
    mg.member = member;
    mg.generation = generation;
    mg.roleInGen = role;
    return mg;
  }

  @PrePersist
  void prePersist() {
    this.joinedAt = Instant.now();
  }
}
