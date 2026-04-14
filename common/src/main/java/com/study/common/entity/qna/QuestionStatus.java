package com.study.common.entity.qna;

public enum QuestionStatus {

    OPEN("답변 대기 중"),
    RESOLVED("해결됨"),
    CLOSED("종료됨");

    private final String description;

    QuestionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 현재 상태에서 {@code next} 상태로 전이 가능한지 검사한다.
     *
     * <ul>
     *   <li>OPEN → RESOLVED, CLOSED 가능
     *   <li>RESOLVED → CLOSED만 가능
     *   <li>CLOSED → 불가
     * </ul>
     */
    public boolean canTransitionTo(QuestionStatus next) {
        return switch (this) {
            case OPEN -> next == RESOLVED || next == CLOSED;
            case RESOLVED -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}
