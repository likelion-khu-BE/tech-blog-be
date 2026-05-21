package com.study.sessionboard.application.event;

import com.study.profile.application.GenerationService;
import com.study.profile.application.MemberService;
import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostImage;
import com.study.sessionboard.domain.event.EventPostStatus;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.infrastructure.event.EventPostCommentRepository;
import com.study.sessionboard.infrastructure.event.EventPostImageRepository;
import com.study.sessionboard.infrastructure.event.EventPostRepository;
import com.study.sessionboard.presentation.dto.EventPostCreateRequest;
import com.study.sessionboard.presentation.dto.EventPostResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPostService {

  private final EventPostRepository eventPostRepository;
  private final EventPostImageRepository eventPostImageRepository;
  private final EventPostCommentRepository eventPostCommentRepository;
  private final MemberService memberService;
  private final GenerationService generationService;

  public PageWrapper<EventPostSummaryResponse> getEventPosts(
      Integer generationNumber, EventPostType type, Pageable pageable) { // generation number로 시현 수정

    Page<EventPost> posts =
        eventPostRepository.findAllWithFilters(
            generationNumber, EventPostStatus.PUBLISHED, type, pageable);

    List<Long> postIds = posts.map(EventPost::getId).toList();

    // 썸네일: 게시글당 첫 번째 이미지만 배치 조회
    Map<Long, String> thumbMap = new HashMap<>();
    if (!postIds.isEmpty()) {
      eventPostImageRepository
          .findFirstImagesByPostIdIn(postIds)
          .forEach(img -> thumbMap.put(img.getPost().getId(), img.getUrl()));
    }

    // 댓글 수: 게시글별 배치 조회
    Map<Long, Integer> commentCountMap = new HashMap<>();
    if (!postIds.isEmpty()) {
      eventPostCommentRepository
          .countByPostIdIn(postIds)
          .forEach(row -> commentCountMap.put((Long) row[0], ((Long) row[1]).intValue()));
    }

    Page<EventPostSummaryResponse> responsePage =
        posts.map(
            post ->
                EventPostSummaryResponse.of(
                    post,
                    thumbMap.get(post.getId()),
                    commentCountMap.getOrDefault(post.getId(), 0)));

    return PageWrapper.from(responsePage);
  }

  public EventPostResponse getEventPost(Long eventPostId) {
    EventPost post =
        eventPostRepository
            .findById(eventPostId)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

    List<EventPostImage> images =
        eventPostImageRepository.findAllByPostIdOrderByOrderAsc(eventPostId);

    return EventPostResponse.of(post, images);
  }

  @Transactional
  public Long createEventPost(
      Long userId, Integer generationNumber, EventPostCreateRequest request) {
    Member author = memberService.getMemberToUserId(userId);
    Generation generation = generationService.getGenerationByNumber(generationNumber);

    EventPost post =
        EventPost.of(
            author,
            generation,
            EventPostType.valueOf(request.type()),
            request.title(),
            request.body(),
            request.tags() != null ? request.tags().toArray(new String[0]) : null);

    return eventPostRepository.save(post).getId();
  }

  @Transactional
  public void updateEventPost(Long userId, Long eventPostId, EventPostCreateRequest request) {
    EventPost post =
        eventPostRepository
            .findById(eventPostId)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

    if (!post.getAuthor().getUser().getId().equals(userId)) {
      throw new IllegalStateException("본인 게시글만 수정할 수 있습니다.");
    }

    post.update(
        EventPostType.valueOf(request.type()),
        request.title(),
        request.body(),
        request.tags() != null ? request.tags().toArray(new String[0]) : null);
  }

  @Transactional
  public void deleteEventPost(Long userId, Long eventPostId) {
    EventPost post =
        eventPostRepository
            .findById(eventPostId)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

    if (!post.getAuthor().getUser().getId().equals(userId)) {
      throw new IllegalStateException("본인 게시글만 삭제할 수 있습니다.");
    }

    eventPostRepository.delete(post);
  }
}
