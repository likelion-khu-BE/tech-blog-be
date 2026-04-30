package com.study.sessionboard.presentation;

import com.study.profile.domain.member.Member;
import com.study.sessionboard.application.EventPostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/event-posts")
@RequiredArgsConstructor
public class EventPostController {

  private final EventPostService postService;

  // 1. 이벤트 게시글 작성 / 발행 (로그인 사용자)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EventPostResponse> create(
      @RequestPart("data") @Valid EventPostRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      @AuthenticationPrincipal Member loginMember) { // 💡 커스텀 UserDetails 객체 사용 가정

    // 시큐리티 컨텍스트에서 안전하게 유저 객체(또는 ID) 꺼내기
    return ResponseEntity.ok(postService.create(request, images, loginMember));
  }

  // 2. 이벤트 게시글 상세 조회 (이미지 포함)
  @GetMapping("/{postId}")
  public ResponseEntity<EventPostResponse> getOne(@PathVariable Long postId) {
    // ID 타입이 UUID에서 Long으로 변경됨 (엔티티 기준)
    return ResponseEntity.ok(postService.getOne(postId));
  }

  // 3. 이벤트 게시글 수정 (추가 구현 필요 피드백 반영)
  @PutMapping("/{postId}")
  public ResponseEntity<EventPostResponse> update(
      @PathVariable Long postId,
      @Valid @RequestBody EventPostRequest request,
      @AuthenticationPrincipal Member loginMember) {

    return ResponseEntity.ok(postService.update(postId, request, loginMember));
  }
}
