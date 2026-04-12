package com.study.common.repository;

import com.study.common.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByLoginEmail(String loginEmail);

  boolean existsByLoginEmail(String loginEmail);
}
