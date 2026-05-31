package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Resource;
import com.study.sessionboard.domain.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
  long countBySession(Session session);
  List<Resource> findAllBySession(Session session);
  List<Resource> findAllBySessionAndType(Session session, String type);
}
