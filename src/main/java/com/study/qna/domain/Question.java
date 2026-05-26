package com.study.qna.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity(name = "QnaQuestion")
@Table(name = "question")
@Getter
@DynamicUpdate
@SQLDelete(sql = "UPDATE question SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * QnA 질문 도메인 엔티티.
 *
 * <p>질문 본문/상태/조회수/답변수/태그 연관을 관리한다. 작성자는 userId(Long)만 저장해 타 BC 엔티티와 직접 결합하지 않는다.
 */
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "text")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private QuestionStatus status;

  @Column(name = "generation", nullable = false)
  private int generation;

  @Column(name = "view_count", nullable = false)
  private int viewCount = 0;

  @Column(name = "answer_count", nullable = false)
  private int answerCount = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuestionTag> questionTags = new ArrayList<>();

  public static Question create(Long userId, String title, String content, int generation) {
    Question question = new Question();
    question.userId = userId;
    question.title = title;
    question.content = content;
    question.status = QuestionStatus.OPEN;
    question.generation = generation;
    return question;
  }

  public void update(String title, String content) {
    if (title != null) {
      this.title = title;
    }
    if (content != null) {
      this.content = content;
    }
  }

  public void resolve() {
    if (!this.status.canTransitionTo(QuestionStatus.RESOLVED)) {
      throw new IllegalStateException("질문 상태를 RESOLVED로 변경할 수 없습니다");
    }
    this.status = QuestionStatus.RESOLVED;
  }

  public void reopen() {
    this.status = QuestionStatus.OPEN;
  }

  public void addTag(Tag tag) {
    boolean exists =
        this.questionTags.stream()
            .anyMatch(questionTag -> questionTag.getTag().getId().equals(tag.getId()));
    if (!exists) {
      this.questionTags.add(QuestionTag.create(this, tag));
    }
  }

  public boolean isAuthor(Long userId) {
    return this.userId.equals(userId);
  }

  public void incrementViewCount() {
    this.viewCount += 1;
  }

  public void incrementAnswerCount() {
    this.answerCount += 1;
  }

  public void decrementAnswerCount() {
    if (this.answerCount > 0) {
      this.answerCount -= 1;
    }
  }

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
