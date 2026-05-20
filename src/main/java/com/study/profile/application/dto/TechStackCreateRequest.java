package com.study.profile.application.dto;

import com.study.profile.domain.techstack.TechStackCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 기술 스택 등록/수정 요청 DTO (§3-2, §3-3 공용).
 *
 * <p>logoUrl은 외부 CDN(devicon 등) 주소를 그대로 받는다 — 기술 스택 로고는 사용자 업로드 이미지가 아니라 정해진 아이콘 라이브러리 링크이므로 S3 업로드
 * 흐름을 타지 않는다.
 */
public record TechStackCreateRequest(
    @Schema(example = "Bun") @NotBlank String name,
    @Schema(example = "framework") @NotNull TechStackCategory category,
    @Schema(
            example =
                "https://raw.githubusercontent.com/devicons/devicon/master/icons/bun/bun-original.svg")
        String logoUrl) {}
