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

/**
 * 기수(Generation) 엔티티.
 *
 * 멋쟁이사자처럼의 '기수'를 나타낸다. 예) 13기, 14기, 15기.
 * 각 기수는 활동 시작일과 종료일이 있으며, 현재 활동 중인 기수인지 여부를 관리한다.
 *
 * [DB 테이블: generation]
 * 이 클래스의 필드 하나하나가 DB 테이블의 컬럼(열) 하나씩에 대응된다.
 */
@Entity
@Table(
    name = "generation",
    indexes = {
      // number 컬럼에 인덱스를 걸어 기수 번호로 빠르게 검색하고, 중복 기수 번호를 방지한다.
      @Index(name = "idx_generation_number", columnList = "number", unique = true)
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;              // DB가 자동으로 부여하는 고유 식별 번호

  @Column(name = "label", nullable = false)
  private String label;         // 기수 표시명 (예: "13기", "14기")

  @Column(name = "number", nullable = false, unique = true)
  private Integer number;       // 기수 번호 (예: 13, 14) — 중복 불가

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;  // 기수 활동 시작일 (날짜만, 시각 제외)

  @Column(name = "end_date")
  private LocalDate endDate;    // 기수 활동 종료일 (진행 중이면 null)

  @Column(name = "is_current", nullable = false)
  private Boolean isCurrent = false;  // 현재 활동 중인 기수이면 true

  // updatable = false : 최초 저장 시각은 한 번만 기록하고 이후 변경하지 않는다.
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;    // 기수 데이터 생성 시각

  /**
   * 새 기수를 생성할 때 호출하는 정적 팩토리 메서드.
   * 처음 만들 때는 isCurrent = false로 시작하고, 필요 시 markAsCurrent()로 변경한다.
   */
  public static Generation create(String label, Integer number, LocalDate startDate) {
    Generation generation = new Generation();
    generation.label = label;
    generation.number = number;
    generation.startDate = startDate;
    generation.isCurrent = false;
    return generation;
  }

  /**
   * 이 기수를 '현재 활동 중인 기수'로 지정한다.
   * 새 기수가 시작될 때 호출한다.
   */
  public void markAsCurrent() {
    this.isCurrent = true;
  }

  /**
   * 기수 활동을 종료할 때 호출한다.
   * 종료일을 기록하고 isCurrent를 false로 바꾼다.
   */
  public void close(LocalDate endDate) {
    this.endDate = endDate;
    this.isCurrent = false;
  }

  /** DB에 저장하기 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
