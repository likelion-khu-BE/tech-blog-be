package com.study.sessionboard.infrastructure.session;

import com.study.sessionboard.domain.session.Retro;
import com.study.sessionboard.domain.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RetroRepository extends JpaRepository<Retro, Long> {
  @Query("SELECT AVG(r.rating) FROM Retro r WHERE r.session = :session")
  Double getAverageRatingBySession(@Param("session") Session session);
}
