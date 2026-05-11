package com.study.profile.infrastructure;

import com.study.profile.domain.activity.ActivityFailure;
import org.springframework.data.jpa.repository.JpaRepository;

/** ActivityFailure 영구 로그 Repository. */
public interface ActivityFailureRepository extends JpaRepository<ActivityFailure, Long> {}
