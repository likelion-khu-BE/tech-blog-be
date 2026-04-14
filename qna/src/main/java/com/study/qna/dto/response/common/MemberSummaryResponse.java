package com.study.qna.dto.response.common;

import com.study.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작성자 요약 정보 DTO.
 *
 * <p>nickname, generation은 User 엔티티에 없으므로 서비스 레이어에서 별도로 주입하거나
 * Profile 모듈 연계를 통해 채워야 한다. 현재는 id만 User에서 직접 매핑한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryResponse {

    private Long id;
    private String nickname;
    private int generation;

    /** User 엔티티로부터 생성. nickname/generation은 별도 주입 필요. */
    public static MemberSummaryResponse from(User user) {
        return MemberSummaryResponse.builder()
            .id(user.getId())
            .nickname(user.getLoginEmail()) // TODO: Profile 모듈에서 nickname 조회 필요
            .generation(0)                 // TODO: Profile 모듈에서 generation 조회 필요
            .build();
    }

    public static MemberSummaryResponse of(Long id, String nickname, int generation) {
        return MemberSummaryResponse.builder()
            .id(id)
            .nickname(nickname)
            .generation(generation)
            .build();
    }
}
