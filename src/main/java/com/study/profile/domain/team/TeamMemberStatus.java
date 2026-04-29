package com.study.profile.domain.team;

/**
 * 팀원의 가입 상태.
 *
 * <p>pending : 조장이 초대했고 수락 대기 중
 * <p>accepted : 수락 완료, 활동 중인 정식 팀원 (조장은 팀 생성과 동시에 이 상태)
 * <p>rejected : 초대를 거절함
 * <p>left : 자기 의지로 팀에서 빠짐
 * <p>kicked : 조장이 강퇴함
 */
public enum TeamMemberStatus {
  pending,
  accepted,
  rejected,
  left,
  kicked
}
