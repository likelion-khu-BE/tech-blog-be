package com.study.profile.domain.activity;

/**
 * 기여도(활동 점수 합산)를 조회할 때 사용하는 기간 단위를 나타내는 열거형(Enum).
 *
 * <p>예) "이번 달 기여도 TOP 5", "올해 기여도 TOP 5" 같은 조회에 사용된다.
 *
 * <p>month : 최근 1개월 three_month : 최근 3개월 year : 최근 1년 all : 전체 기간
 */
public enum ContributionPeriodType {
  month,
  three_month,
  year,
  all
}
