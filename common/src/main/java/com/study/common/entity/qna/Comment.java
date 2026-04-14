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
@SQLDelete(sql = "UPDATE comment SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(
    name = "comment",
    indexes = {
      @Index(name = "idx_comment_question_parent", columnList = "question_id, parent_id"),
      @Index(name = "idx_comment_answer_parent", columnList = "answer_id, parent_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // ── 팩토리 메서드 ──

    /** 질문에 대한 루트 댓글을 생성한다. */
    public static Comment createForQuestion(String content, User member, Question question) {
        Comment comment = new Comment();
        comment.content = content;
        comment.member = member;
        comment.question = question;
        return comment;
    }

    /** 답변에 대한 루트 댓글을 생성한다. */
    public static Comment createForAnswer(String content, User member, Answer answer) {
        Comment comment = new Comment();
        comment.content = content;
        comment.member = member;
        comment.answer = answer;
        return comment;
    }

    /** 대댓글을 생성한다. 대댓글(parent.parent != null)에 대한 대댓글은 허용하지 않는다. */
    public static Comment createReply(String content, User member, Comment parent) {
        if (parent.getParent() != null) {
            throw new IllegalStateException("대댓글에는 대댓글을 달 수 없습니다.");
        }
        Comment comment = new Comment();
        comment.content = content;
        comment.member = member;
        comment.parent = parent;
        // 부모의 대상(question/answer)을 상속한다
        comment.question = parent.getQuestion();
        comment.answer = parent.getAnswer();
        return comment;
    }

    // ── 도메인 메서드 ──

    public void update(String content) {
        this.content = content;
    }

    public boolean isAuthor(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public boolean isRootComment() {
        return this.parent == null;
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
