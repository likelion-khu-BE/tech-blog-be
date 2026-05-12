package com.study.qna.infrastructure;

import com.study.qna.application.dto.request.question.QuestionSearchCondition;
import com.study.qna.domain.Question;
import com.study.qna.domain.QuestionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public List<Question> searchQuestions(QuestionSearchCondition condition) {
    List<Long> ids = getPagedIds(condition);
    if (ids.isEmpty()) {
      return List.of();
    }

    List<Question> questions =
        em.createQuery(
                """
                SELECT DISTINCT q FROM QnaQuestion q
                LEFT JOIN FETCH q.questionTags qt
                LEFT JOIN FETCH qt.tag
                WHERE q.id IN :ids
                """,
                Question.class)
            .setParameter("ids", ids)
            .getResultList();

    Map<Long, Question> questionMap =
        questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
    return ids.stream().map(questionMap::get).filter(Objects::nonNull).toList();
  }

  @Override
  public long countQuestions(QuestionSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("SELECT COUNT(q) FROM QnaQuestion q");
    List<String> predicates = buildPredicates(condition);

    if (condition.tagId() != null) {
      jpql.append(" JOIN q.questionTags qt");
    }
    if (!predicates.isEmpty()) {
      jpql.append(" WHERE ").append(String.join(" AND ", predicates));
    }

    TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
    applyParameters(query, condition);
    return query.getSingleResult();
  }

  private List<Long> getPagedIds(QuestionSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("SELECT q.id FROM QnaQuestion q");
    List<String> predicates = buildPredicates(condition);

    if (condition.tagId() != null) {
      jpql.append(" JOIN q.questionTags qt");
    }
    if (!predicates.isEmpty()) {
      jpql.append(" WHERE ").append(String.join(" AND ", predicates));
    }

    jpql.append(" ORDER BY ").append(orderClause(condition.sort()));

    TypedQuery<Long> query =
        em.createQuery(jpql.toString(), Long.class)
            .setFirstResult(condition.page() * condition.size())
            .setMaxResults(condition.size());
    applyParameters(query, condition);
    return query.getResultList();
  }

  private List<String> buildPredicates(QuestionSearchCondition condition) {
    List<String> predicates = new ArrayList<>();

    if (condition.status() != null && !condition.status().isBlank()) {
      predicates.add("q.status = :status");
    }
    if (condition.generation() != null) {
      predicates.add("q.generation = :generation");
    }
    if (condition.keyword() != null && !condition.keyword().isBlank()) {
      predicates.add(
          "(LOWER(q.title) LIKE LOWER(:keyword) OR LOWER(q.content) LIKE LOWER(:keyword))");
    }
    if (condition.tagId() != null) {
      predicates.add("qt.tag.id = :tagId");
    }

    return predicates;
  }

  private String orderClause(String sort) {
    return switch (sort) {
      case "vote" -> "q.answerCount DESC, q.createdAt DESC";
      case "unanswered" -> "q.answerCount ASC, q.createdAt DESC";
      default -> "q.createdAt DESC";
    };
  }

  private void applyParameters(TypedQuery<?> query, QuestionSearchCondition condition) {
    if (condition.status() != null && !condition.status().isBlank()) {
      query.setParameter("status", QuestionStatus.valueOf(condition.status()));
    }
    if (condition.generation() != null) {
      query.setParameter("generation", condition.generation());
    }
    if (condition.keyword() != null && !condition.keyword().isBlank()) {
      query.setParameter("keyword", "%" + condition.keyword() + "%");
    }
    if (condition.tagId() != null) {
      query.setParameter("tagId", condition.tagId());
    }
  }
}
