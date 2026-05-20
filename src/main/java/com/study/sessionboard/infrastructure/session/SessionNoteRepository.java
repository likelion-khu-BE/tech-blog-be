package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionNoteRepository extends JpaRepository<SessionNote, Long> {
  long countBySession(Session session);
}
