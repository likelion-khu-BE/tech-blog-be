package com.study.blog.infrastructure.admin;

import com.study.blog.domain.admin.AdminActionLog;
import com.study.blog.domain.admin.AdminTargetType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

  Page<AdminActionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

  List<AdminActionLog> findAllByTargetTypeAndTargetIdOrderByCreatedAtDesc(
      AdminTargetType targetType, String targetId);
}
