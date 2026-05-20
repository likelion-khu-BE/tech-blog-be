package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionSpeaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionSpeakerRepository extends JpaRepository<SessionSpeaker, Long> {
  List<SessionSpeaker> findAllBySession(Session session);

  void deleteAllBySession(Session session);
}
