package com.study.sessionboard.infrastructure;

import com.study.sessionboard.domain.event.EventPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventPostRepository extends JpaRepository<EventPost, Long> {}
