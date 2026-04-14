package com.study.common.entity.qna;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "question_tag",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_question_tag", columnNames = {"question_id", "tag_id"})
    },
    indexes = {
      @Index(name = "idx_question_tag_tag", columnList = "tag_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
