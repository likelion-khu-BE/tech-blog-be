package com.study.profile.infrastructure;

import com.study.profile.domain.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Activity 엔티티 Repository — 활동 이력 저장. */
public interface ActivityRepository extends JpaRepository<Activity, Long> {}
