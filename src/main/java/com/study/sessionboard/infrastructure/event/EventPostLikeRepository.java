package com.study.sessionboard.infrastructure.event;

import com.study.sessionboard.domain.event.EventPostLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventPostLikeRepository extends JpaRepository<EventPostLike, Long> {

    Optional<EventPostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}