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

/**
 * 멤버-기수 연결 엔티티 (중간 테이블).
 *
 * <p>"어떤 멤버가 몇 기에 어떤 역할로 참여했는가"를 기록한다.
 *
 * <p>[왜 중간 테이블이 필요한가?] 멤버와 기수는 다대다(N:M) 관계이다. 한 멤버는 여러 기수에 참여할 수 있고 (13기, 14기 운영진 등), 한 기수에는 여러 멤버가
 * 참여한다. DB에서는 이런 다대다 관계를 직접 표현할 수 없어서, 이 MemberGeneration 같은 중간 테이블을 두어 '멤버 id + 기수 id' 쌍으로 관계를
 * 기록한다.
 *
 * <p>[DB 테이블: member_generation]
 */
@Entity
@Table(name = "member_generation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGeneration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // 이 연결 행의 고유 번호

  // @ManyToOne : 여러 MemberGeneration 행이 하나의 Member를 참조할 수 있다 (N:1 관계).
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member; // 참여한 멤버

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "generation_id", nullable = false)
  private Generation generation; // 참여한 기수

  @Enumerated(EnumType.STRING)
  @Column(name = "role_in_gen", nullable = false)
  private GenerationRole roleInGen = GenerationRole.member; // 해당 기수에서의 역할 (일반멤버/운영진)

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt; // 이 기수에 합류한 시각

  /** 특정 멤버를 특정 기수에 특정 역할로 연결하는 정적 팩토리 메서드. (예: "홍길동을 13기 운영진으로 등록") */
  public static MemberGeneration create(Member member, Generation generation, GenerationRole role) {
    MemberGeneration mg = new MemberGeneration();
    mg.member = member;
    mg.generation = generation;
    mg.roleInGen = role;
    return mg;
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 joinedAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.joinedAt = Instant.now();
  }
}
