package com.study.common.entity;

/**
 * 특정 기수(Generation)에서 멤버가 맡은 역할을 나타내는 열거형(Enum).
 *
 * 같은 사람이라도 기수마다 역할이 다를 수 있다.
 * (예: 13기에는 일반 멤버였다가 14기에는 운영진이 될 수 있음)
 *
 * member    : 일반 멤버
 * operating : 운영진 (회장/부회장, 부서별 운영진, 파트장 포함 전부)
 */
public enum GenerationRole {
    member, operating
}
