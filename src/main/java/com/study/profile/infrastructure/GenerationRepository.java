package com.study.profile.infrastructure;

import com.study.profile.domain.generation.Generation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GenerationRepository extends JpaRepository<Generation, Integer> {

  List<Generation> findAllByOrderByNumberAsc();

  @Query("SELECT g FROM Generation g WHERE g.isCurrent = true")
  Optional<Generation> findCurrentGeneration();
}
