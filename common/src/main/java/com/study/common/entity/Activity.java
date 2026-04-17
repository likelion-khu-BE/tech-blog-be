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
 * 멤버 활동 이력 엔티티.
 *
 * <p>멤버가 블로그 글 작성, Q&A 답변, 세션 발표 등 활동을 할 때마다 그 기록을 이 테이블에 한 행씩 쌓는다. 각 활동에는 점수(score)가 부여되어 기여도 랭킹
 * 등에 활용된다.
 *
 * <p>[예시 데이터] member_id=1(홍길동), type=blog_post, score=10 → "홍길동이 블로그 글을 써서 10점 획득"
 * member_id=1(홍길동), type=qna_accepted, score=5 → "홍길동의 답변이 채택되어 5점 획득"
 *
 * <p>[DB 테이블: activity]
 */
@Entity
@Table(name = "activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // DB가 자동으로 부여하는 고유 번호

  // 어떤 멤버의 활동인지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ActivityType type; // 활동 종류 (blog_post/../other)

  // 이 서비스(profile 모듈)가 아닌 외부 모듈(blog, qna 등)에서 관리하는 ID이므로
  @Column(name = "reference_id")
  private Long referenceId;

  // 참조 타입 이름. 어떤 종류의 콘텐츠를 가리키는지 구분하는 문자열.
  // 예) "blog_post", "qna_answer" — referenceId와 함께 "어디서 온 ID인지" 맥락을 제공한다.
  @Column(name = "reference_type")
  private String referenceType;

  @Column(name = "score", nullable = false)
  private Integer score = 0; // 이 활동으로 얻은 점수 (기본값 0)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 활동이 기록된 시각

  /**
   * 새 활동 기록을 생성할 때 사용하는 정적 팩토리 메서드.
   *
   * <p>예) Activity.create(홍길동, ActivityType.blog_post, 글UUID, "blog_post", 10) → "홍길동이 해당 블로그 글을
   * 작성해 10점 획득"을 기록
   */
  public static Activity create(
      Member member, ActivityType type, Long referenceId, String referenceType, int score) {
    Activity activity = new Activity();
    activity.member = member;
    activity.type = type;
    activity.referenceId = referenceId;
    activity.referenceType = referenceType;
    activity.score = score;
    return activity;
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
