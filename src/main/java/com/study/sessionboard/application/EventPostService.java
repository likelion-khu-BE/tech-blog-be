package com.study.sessionboard.application;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostImage;
import com.study.sessionboard.infrastructure.EventPostImageRepository;
import com.study.sessionboard.infrastructure.EventPostRepository;
import com.study.sessionboard.presentation.EventPostRequest;
import com.study.sessionboard.presentation.EventPostResponse;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 readOnly, 쓰기 메서드만 @Transactional 별도
public class EventPostService {

  private final EventPostRepository postRepository;
  private final EventPostImageRepository imageRepository;

  // 작성 / 발행
  @Transactional
  public EventPostResponse create(
      EventPostRequest request, List<MultipartFile> images, Member loginMember) {

    // Generation은 Member가 속한 기수를 쓰거나 request.generationId로 조회
    // 지금은 Member의 currentGeneration을 쓴다고 가정
    // 기존 코드
    // Generation generation = loginMember.getCurrentGeneration();

    // 에러 회피용 임시 처리
    // TODO: [프로필 팀] 작성자의 현재 기수(Generation)를 가져오는 로직 추가 필요
    Generation generation = null;

    EventPost post = EventPost.of(loginMember, generation, request.getType(), request.getTitle());

    // EventPost 엔티티에 body/tags/status setter가 없어서 지금은 title만 저장됨
    // 엔티티에 update 메서드 추가되면 아래 주석 해제
    // post.updateContent(request.getBody(), request.getTags(), request.getStatus());

    EventPost saved = postRepository.save(post);

    // 이미지 저장 (URL은 실제로는 S3 업로드 후 받아야 함 — 지금은 파일명으로 임시 저장)
    if (images != null && !images.isEmpty()) {
      List<EventPostImage> postImages =
          images.stream()
              .filter(f -> f != null && !f.isEmpty())
              .map(f -> EventPostImage.of(saved, Objects.requireNonNull(f.getOriginalFilename())))
              .toList();
      imageRepository.saveAll(postImages);
    }

    List<String> imageUrls =
        imageRepository.findByPostIdOrderByOrder(saved.getId()).stream()
            .map(EventPostImage::getUrl)
            .toList();

    return EventPostResponse.from(saved, imageUrls);
  }

  // 상세 조회 (이미지 포함)
  public EventPostResponse getOne(Long postId) {
    EventPost post =
        postRepository
            .findWithDetailsById(postId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

    List<String> imageUrls =
        imageRepository.findByPostIdOrderByOrder(postId).stream()
            .map(EventPostImage::getUrl)
            .toList();

    return EventPostResponse.from(post, imageUrls);
  }

  // 수정 (엔티티 update 메서드 추가 대기 중)
  @Transactional
  public EventPostResponse update(Long postId, EventPostRequest request, Member loginMember) {
    EventPost post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

    // 작성자 본인인지 확인 (403)
    if (!Objects.equals(post.getAuthor().getId(), loginMember.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다.");
    }

    // 엔티티에 updateContent() 추가되면 여기서 호출
    // post.updateContent(request.getTitle(), request.getBody(),
    //     request.getTags(), request.getStatus());
    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "게시글 수정은 아직 구현 중입니다.");
  }

  // 삭제
  @Transactional
  public void delete(Long postId, Member loginMember) {
    EventPost post =
        postRepository
            .findById(postId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

    if (!Objects.equals(post.getAuthor().getId(), loginMember.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 삭제 권한이 없습니다.");
    }

    postRepository.delete(post);
  }
}
