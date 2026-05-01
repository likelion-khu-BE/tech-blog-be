package com.study.profile.infrastructure;

import com.study.profile.domain.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Activity 엔티티 Repository.
 *
 * <p>활동 이력 저장. Spring Data JPA 표준 메서드(save, findById 등) 기본 제공. 조회용 커스텀 메서드는 별도 read 영역 PR에서 추가 예정.
 */
public interface ActivityRepository extends JpaRepository<Activity, Long> {}
