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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 멤버-기술스택 연결 엔티티 (중간 테이블).
 *
 * <p>"어떤 멤버가 어떤 기술을 어느 정도 수준으로 다루는가"를 기록한다.
 *
 * <p>[왜 중간 테이블인가?] 멤버와 기술 스택도 다대다(N:M) 관계다. 한 멤버는 Java, Spring, Docker 등 여러 기술을 가질 수 있고, 하나의 기술(예:
 * Java)은 여러 멤버가 동시에 보유할 수 있다. 이 MemberTechStack이 "멤버 A는 Java를 숙련도 5로 사용한다"는 관계를 하나의 행으로 표현한다.
 *
 * <p>[DB 테이블: member_tech_stack]
 */
@Entity
@Table(
    name = "member_tech_stack",
    // 같은 멤버가 같은 기술 스택을 중복 등록하지 못하도록 복합 유니크 제약을 건다.
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_member_tech_stack",
            columnNames = {"member_id", "tech_stack_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // 이 연결 행의 고유 번호

  // 어떤 멤버인지 (member_id 외래키로 Member 테이블 참조)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  // 어떤 기술 스택인지 (tech_stack_id 외래키로 TechStack 테이블 참조)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tech_stack_id")
  private TechStack techStack;

  // 해당 기술의 숙련도 (예: 1~5 또는 1~10 — null이면 숙련도 미입력).
  // DB 컬럼 제약이 별도로 없으므로 숫자 범위는 서비스 레이어에서 검증한다.
  @Column(name = "proficiency")
  private Integer proficiency;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 기술 스택 등록 시각

  /** 멤버에게 기술 스택을 등록할 때 사용하는 정적 팩토리 메서드. (예: MemberTechStack.create(member, javaStack, 4)) */
  public static MemberTechStack create(Member member, TechStack techStack, Integer proficiency) {
    MemberTechStack mts = new MemberTechStack();
    mts.member = member;
    mts.techStack = techStack;
    mts.proficiency = proficiency;
    return mts;
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
