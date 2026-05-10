package com.study.qna.infrastructure;

import com.study.qna.domain.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  boolean existsByName(String name);

  List<Tag> findAllByOrderByNameAsc();

  List<Tag> findAllByIdIn(List<Long> ids);
}
