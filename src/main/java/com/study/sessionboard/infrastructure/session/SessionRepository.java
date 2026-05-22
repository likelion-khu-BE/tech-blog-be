package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
  List<Session> findAllByGenerationNumber(Integer generationNumber);

  List<Session> findAllByGenerationNumberAndStatus(Integer generationNumber, SessionStatus status);

  Optional<Session> findByGenerationNumberAndId(Integer generationNumber, Long id);
}
