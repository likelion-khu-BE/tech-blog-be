package com.study.common.entity.techstack;

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

/**
 * 기술 스택 엔티티.
 *
 * <p>서비스에서 사용 가능한 기술 스택의 '목록(카탈로그)' 역할을 한다. 멤버가 자신의 기술 스택을 등록할 때 이 목록에서 골라서 연결한다 (→ MemberTechStack
 * 참고).
 *
 * <p>예) id=1, name="Java", category=language id=2, name="Spring", category=backend id=3,
 * name="React", category=frontend
 *
 * <p>[DB 테이블: tech_stack]
 */
@Entity
@Table(
    name = "tech_stack",
    indexes = {
      // name 컬럼에 인덱스를 걸어 이름으로 빠르게 찾고, 같은 이름의 기술이 중복 등록되지 않게 막는다.
      @Index(name = "idx_tech_stack_name", columnList = "name", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // DB가 자동으로 부여하는 고유 번호

  @Column(name = "name", nullable = false, unique = true)
  private String name; // 기술 스택 이름 (예: "Java", "React") — 중복 불가

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private TechStackCategory category; // 분류 (language/framework/ai/design/tool/infra/etc)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 등록 시각

  /**
   * 새 기술 스택을 목록에 추가할 때 사용하는 정적 팩토리 메서드. (예: TechStack.create("Kotlin", TechStackCategory.language))
   */
  public static TechStack create(String name, TechStackCategory category) {
    TechStack techStack = new TechStack();
    techStack.name = name;
    techStack.category = category;
    return techStack;
  }

  public void update(String name, TechStackCategory category) {
    this.name = name;
    this.category = category;
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
