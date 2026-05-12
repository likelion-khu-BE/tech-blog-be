package com.study.profile.infrastructure;

import com.study.profile.domain.generation.Generation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {}
