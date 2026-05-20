package com.study.qna.infrastructure;

import com.study.qna.domain.Vote;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

  Optional<Vote> findByAnswer_IdAndUserId(Long answerId, Long userId);

  void deleteAllByAnswer_Id(Long answerId);
}
