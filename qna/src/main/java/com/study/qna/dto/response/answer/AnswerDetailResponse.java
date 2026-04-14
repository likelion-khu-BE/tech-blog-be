package com.study.qna.dto.response.answer;

import com.study.common.entity.qna.Answer;
import com.study.qna.dto.response.common.MemberSummaryResponse;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDetailResponse {

    private Long id;
    private String content;
    private boolean accepted;
    /**
     * 추천 수. vote_count(추천-비추천 합계)가 아닌 실제 UPVOTE 집계를 사용하려면
     * 서비스 레이어에서 {@link #of(Answer, int, int)}를 호출해야 한다.
     */
    private int upvoteCount;
    /** 비추천 수. 서비스 레이어에서 Vote 테이블을 집계해 주입한다. */
    private int downvoteCount;
    private int commentCount;
    private MemberSummaryResponse author;
    private Instant createdAt;

    /**
     * Answer 엔티티만으로 생성. upvoteCount = voteCount(net), downvoteCount = 0.
     * 정확한 up/down 분리가 필요하면 {@link #of(Answer, int, int)}를 사용한다.
     */
    public static AnswerDetailResponse from(Answer answer) {
        return of(answer, answer.getVoteCount(), 0);
    }

    /** up/down 집계 값을 서비스 레이어에서 직접 주입할 때 사용한다. */
    public static AnswerDetailResponse of(Answer answer, int upvoteCount, int downvoteCount) {
        return AnswerDetailResponse.builder()
            .id(answer.getId())
            .content(answer.getContent())
            .accepted(answer.isAccepted())
            .upvoteCount(upvoteCount)
            .downvoteCount(downvoteCount)
            .commentCount(answer.getCommentCount())
            .author(MemberSummaryResponse.from(answer.getMember()))
            .createdAt(answer.getCreatedAt())
            .build();
    }
}
