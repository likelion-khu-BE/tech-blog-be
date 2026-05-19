package com.study.profile.domain.activity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * §6-4 랭킹 점수 합산 기간 필터.
 *
 * <p>{@link #toStartInstant()}는 합산 시작 시점 반환. {@code all}은 {@link Optional#empty()} — 전체 기간이라 시간 조건 X.
 */
public enum RankingPeriod {
  month(30L),
  year(365L),
  all(null);

  private final Long days;

  RankingPeriod(Long days) {
    this.days = days;
  }

  public Optional<Instant> toStartInstant() {
    return Optional.ofNullable(days).map(d -> Instant.now().minus(d, ChronoUnit.DAYS));
  }
}
