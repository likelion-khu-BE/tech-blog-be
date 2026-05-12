package com.study.profile.domain.generation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * <p>멋쟁이사자처럼의 '기수'를 나타낸다. 예) 13기, 14기, 15기. 각 기수는 활동 시작일과 종료일이 있으며, 현재 활동 중인 기수인지 여부를 관리한다.
 *
 * <p>[DB 테이블: generation] 이 클래스의 필드 하나하나가 DB 테이블의 컬럼(열) 하나씩에 대응된다.
 */
@Entity
@Table(name = "generation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation {

  @Id
  @Column(name = "number", nullable = false)
  private Integer number; // 기수 번호 (예: 13, 14) — PK

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate; // 기수 활동 시작일 (날짜만, 시각 제외)

  @Column(name = "end_date")
  private LocalDate endDate; // 기수 활동 종료일 (진행 중이면 null)

  @Column(name = "is_current", nullable = false)
  private Boolean isCurrent = false; // 현재 활동 중인 기수이면 true

  // updatable = false : 최초 저장 시각은 한 번만 기록하고 이후 변경하지 않는다.
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 기수 데이터 생성 시각

  /** 새 기수를 생성할 때 호출하는 정적 팩토리 메서드. 처음 만들 때는 isCurrent = false로 시작하고, 필요 시 markAsCurrent()로 변경한다. */
  public static Generation create(
      Integer number, LocalDate startDate, LocalDate endDate, Boolean isCurrent) {
    Generation generation = new Generation();
    generation.number = number;
    generation.startDate = startDate;
    generation.endDate = endDate;
    generation.isCurrent = isCurrent != null ? isCurrent : false;
    return generation;
  }

  public void update(Integer number, LocalDate startDate, LocalDate endDate, Boolean isCurrent) {
    this.number = number;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isCurrent = isCurrent != null ? isCurrent : false;
  }

  /** 이 기수를 '현재 활동 중인 기수'로 지정한다. */
  public void markAsCurrent() {
    this.isCurrent = true;
  }

  /** 기수 활동을 종료할 때 호출한다. */
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
