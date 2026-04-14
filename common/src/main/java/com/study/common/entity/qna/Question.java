package com.study.common.entity.qna;

import com.study.common.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@SQLDelete(sql = "UPDATE question SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuestionStatus status;

    @Column(name = "generation", nullable = false)
    private int generation;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "answer_count", nullable = false)
    private int answerCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionTag> questionTags = new ArrayList<>();

    // ── 팩토리 메서드 ──

    public static Question create(String title, String content, User member) {
        Question question = new Question();
        question.title = title;
        question.content = content;
        question.member = member;
        question.status = QuestionStatus.OPEN;
        question.generation = 0; // 서비스 레이어에서 member의 generation으로 설정
        question.viewCount = 0;
        question.answerCount = 0;
        return question;
    }

    // ── 도메인 메서드 ──

    public void update(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void resolve() {
        validateTransition(QuestionStatus.RESOLVED);
        this.status = QuestionStatus.RESOLVED;
    }

    public void close() {
        validateTransition(QuestionStatus.CLOSED);
        this.status = QuestionStatus.CLOSED;
    }

    /** 채택 취소 등으로 OPEN으로 되돌린다. CLOSED 상태에서는 불가. */
    public void reopen() {
        if (this.status == QuestionStatus.CLOSED) {
            throw new IllegalStateException("CLOSED 상태의 질문은 재오픈할 수 없습니다.");
        }
        this.status = QuestionStatus.OPEN;
    }

    public boolean isAuthor(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    /** 태그를 연결하는 편의 메서드. QuestionTag를 생성하고 컬렉션에 추가한다. */
    public void addTag(Tag tag) {
        QuestionTag questionTag = QuestionTag.create(this, tag);
        this.questionTags.add(questionTag);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementAnswerCount() {
        this.answerCount++;
    }

    public void decrementAnswerCount() {
        if (this.answerCount > 0) {
            this.answerCount--;
        }
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    private void validateTransition(QuestionStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException(
                "상태 전이 불가: " + this.status + " → " + next);
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
