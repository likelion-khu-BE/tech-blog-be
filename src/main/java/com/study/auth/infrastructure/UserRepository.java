package com.study.auth.infrastructure;

import com.study.auth.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginEmail(String loginEmail);

  boolean existsByLoginEmail(String loginEmail);
}
