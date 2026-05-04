package com.study.auth.infrastructure;

import com.study.auth.domain.User;
import com.study.auth.domain.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginEmail(String loginEmail);

  boolean existsByLoginEmail(String loginEmail);

  List<User> findAllByOrderBySignupRequestedAtDesc();

  List<User> findAllByStatusOrderBySignupRequestedAtDesc(UserStatus status);
}
