package com.study.qna.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(
    name = "question_tag",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_question_tag",
          columnNames = {"question_id", "tag_id"})
    })
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/** 질문-태그 연결 도메인 엔티티. */
public class QuestionTag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tag_id", nullable = false)
  private Tag tag;

  public static QuestionTag create(Question question, Tag tag) {
    QuestionTag questionTag = new QuestionTag();
    questionTag.question = question;
    questionTag.tag = tag;
    return questionTag;
  }
}
