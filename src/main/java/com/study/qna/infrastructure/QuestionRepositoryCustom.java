package com.study.qna.infrastructure;

import com.study.qna.application.dto.request.question.QuestionSearchCondition;
import com.study.qna.domain.Question;
import java.util.List;

public interface QuestionRepositoryCustom {

  List<Question> searchQuestions(QuestionSearchCondition condition);

  long countQuestions(QuestionSearchCondition condition);
}
