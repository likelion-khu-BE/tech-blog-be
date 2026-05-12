package com.study.qna.application.dto.request.question;

/** 질문 목록 동적 검색 조건. */
public record QuestionSearchCondition(
    String keyword,
    String status,
    Long tagId,
    Integer generation,
    String sort,
    int page,
    int size) {}
