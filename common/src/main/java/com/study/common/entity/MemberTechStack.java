package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "member_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tech_stack_id")
  private TechStack techStack;

  private Integer proficiency;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static MemberTechStack create(Member member, TechStack techStack, Integer proficiency) {
    MemberTechStack mts = new MemberTechStack();
    mts.member = member;
    mts.techStack = techStack;
    mts.proficiency = proficiency;
    return mts;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
