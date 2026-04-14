package com.study.common.entity.qna;

import com.study.common.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@DynamicUpdate
@SQLDelete(sql = "UPDATE answer SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(
    name = "answer",
    indexes = {
      @Index(name = "idx_answer_question", columnList = "question_id"),
      @Index(name = "idx_answer_accepted", columnList = "question_id, accepted"),
      @Index(
          name = "idx_answer_question_accepted_vote",
          columnList = "question_id, accepted, vote_count"),
      @Index(name = "idx_answer_member", columnList = "member_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    /** 추천수 - 비추천수 합계 (net vote count). */
    @Column(name = "vote_count", nullable = false)
    private int voteCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // ── 팩토리 메서드 ──

    public static Answer create(String content, Question question, User member) {
        Answer answer = new Answer();
        answer.content = content;
        answer.question = question;
        answer.member = member;
        answer.accepted = false;
        answer.voteCount = 0;
        answer.commentCount = 0;
        return answer;
    }

    // ── 도메인 메서드 ──

    public void update(String content) {
        this.content = content;
    }

    public void accept() {
        this.accepted = true;
    }

    public void cancelAccept() {
        this.accepted = false;
    }

    public boolean isAuthor(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public void applyVote(VoteType type) {
        this.voteCount += (type == VoteType.UPVOTE) ? 1 : -1;
    }

    public void cancelVote(VoteType type) {
        this.voteCount -= (type == VoteType.UPVOTE) ? 1 : -1;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    // ── JPA 콜백 ──

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
