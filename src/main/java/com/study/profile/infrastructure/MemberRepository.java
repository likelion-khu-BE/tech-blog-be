package com.study.profile.infrastructure;

import com.study.profile.domain.member.Member;
import com.study.profile.domain.member.SessionType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

  @Query("SELECT m FROM Member m WHERE m.user.id = :userId")
  Optional<Member> findByUserId(@Param("userId") Long userId);

  @Query("SELECT m FROM Member m WHERE m.user.id IN :userIds")
  List<Member> findAllByUserIdIn(@Param("userIds") Collection<Long> userIds);

  @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.user.id = :userId")
  boolean existsByUserId(@Param("userId") Long userId);

  @Query(
      "SELECT m FROM Member m WHERE (:sessionType IS NULL OR m.sessionType = :sessionType) ORDER BY m.name ASC")
  List<Member> findAllFiltered(@Param("sessionType") SessionType sessionType);

  @Query(
      "SELECT m FROM Member m WHERE m.id IN :ids AND (:sessionType IS NULL OR m.sessionType = :sessionType) ORDER BY m.name ASC")
  List<Member> findAllByIdInFiltered(
      @Param("ids") Collection<Long> ids, @Param("sessionType") SessionType sessionType);
}
