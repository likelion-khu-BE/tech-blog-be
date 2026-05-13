package com.study.profile.domain.activity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 기여도(활동 점수 합산)를 조회할 때 사용하는 기간 단위.
 *
 * <p>예) "이번 달 기여도 TOP 5", "올해 기여도 TOP 5".
 *
 * <p>{@link #toStartInstant()}는 합산 시작 시점 (Instant) 반환. {@code all}은 {@link Optional#empty()} — 전체
 * 기간이라 시간 조건 X.
 */
public enum ContributionPeriodType {
  month(30L),
  three_month(90L),
  year(365L),
  all(null);

  private final Long days;

  ContributionPeriodType(Long days) {
    this.days = days;
  }

  /** 합산 시작 시점 (지금부터 {@code days}일 전). {@code all}이면 {@link Optional#empty()}. */
  public Optional<Instant> toStartInstant() {
    return Optional.ofNullable(days).map(d -> Instant.now().minus(d, ChronoUnit.DAYS));
  }
}
