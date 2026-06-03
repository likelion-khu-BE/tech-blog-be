package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {

  @Query(
      "SELECT sn FROM SessionNote sn WHERE sn.session.id = :sessionId AND (:query IS NULL OR sn.body LIKE %:query%) ORDER BY sn.createdAt DESC")
  List<SessionNote> findAllBySessionIdAndQuery(
      @Param("sessionId") Long sessionId, @Param("query") String query);

  long countBySession(Session session);
}
