package com.study.common.entity.qna;

import com.study.common.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(
    name = "vote",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_vote_answer_member", columnNames = {"answer_id", "member_id"})
    },
    indexes = {
      @Index(name = "idx_vote_answer_type", columnList = "answer_id, type")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private VoteType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Vote create(Answer answer, User member, VoteType type) {
        Vote vote = new Vote();
        vote.answer = answer;
        vote.member = member;
        vote.type = type;
        return vote;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
