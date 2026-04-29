package com.study.sessionboard.application.event;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.sessionboard.application.event.dto.EventPostCreateRequest;
import com.study.sessionboard.application.event.dto.EventPostResponse;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.EventPostUpdateRequest;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostImage;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.infrastructure.event.EventPostImageRepository;
import com.study.sessionboard.infrastructure.event.EventPostRepository;
import com.study.sessionboard.shared.exception.EventPostErrorCode;
import com.study.sessionboard.shared.exception.EventPostException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventPostService {

  private final EventPostRepository eventPostRepository;
  private final EventPostImageRepository eventPostImageRepository;
  private final EntityManager entityManager;

  public EventPostService(
      EventPostRepository eventPostRepository,
      EventPostImageRepository eventPostImageRepository,
      EntityManager entityManager) {
    this.eventPostRepository = eventPostRepository;
    this.eventPostImageRepository = eventPostImageRepository;
    this.entityManager = entityManager;
  }

  public Page<EventPostSummaryResponse> getEventPosts(
      EventPostType type, LocalDate date, Pageable pageable) {
    Page<EventPost> posts = eventPostRepository.findAllWithFilters(type, date, pageable);
    List<Long> postIds = posts.map(EventPost::getId).toList();

    // postId → 첫 번째 이미지 URL 매핑 (배치 조회로 N+1 방지)
    Map<Long, String> thumbnailMap = new HashMap<>();
    if (!postIds.isEmpty()) {
      eventPostImageRepository
          .findByPostIdInOrderByOrderAsc(postIds)
          .forEach(img -> thumbnailMap.putIfAbsent(img.getPost().getId(), img.getUrl()));
    }

    return posts.map(post -> EventPostSummaryResponse.of(post, thumbnailMap.get(post.getId())));
  }

  public EventPostResponse getEventPost(Long postId) {
    EventPost post = findPostOrThrow(postId);
    List<String> imageUrls =
        eventPostImageRepository.findByPostIdOrderByOrderAsc(postId).stream()
            .map(EventPostImage::getUrl)
            .toList();
    return EventPostResponse.of(post, imageUrls);
  }

  @Transactional
  public EventPostResponse createEventPost(EventPostCreateRequest request, Long memberId) {
    Member author = entityManager.getReference(Member.class, memberId);
    Generation generation =
        entityManager.getReference(Generation.class, request.getGenerationId());

    EventPost post =
        EventPost.create(
            author,
            generation,
            request.getType(),
            request.getTitle(),
            request.getBody(),
            request.getEventDate(),
            request.getLocation());
    eventPostRepository.save(post);

    saveImages(post, request.getImageUrls());

    List<String> imageUrls =
        request.getImageUrls() == null ? List.of() : request.getImageUrls();
    return EventPostResponse.of(post, imageUrls);
  }

  @Transactional
  public EventPostResponse updateEventPost(
      Long postId, EventPostUpdateRequest request, Long memberId) {
    EventPost post = findPostOrThrow(postId);
    if (!post.isOwnedBy(memberId)) {
      throw new EventPostException(EventPostErrorCode.FORBIDDEN);
    }

    post.update(
        request.getType(),
        request.getTitle(),
        request.getBody(),
        request.getEventDate(),
        request.getLocation());

    // 이미지 전체 교체
    eventPostImageRepository.deleteByPostId(postId);
    saveImages(post, request.getImageUrls());

    List<String> imageUrls =
        request.getImageUrls() == null ? List.of() : request.getImageUrls();
    return EventPostResponse.of(post, imageUrls);
  }

  @Transactional
  public void deleteEventPost(Long postId, Long memberId) {
    EventPost post = findPostOrThrow(postId);
    if (!post.isOwnedBy(memberId)) {
      throw new EventPostException(EventPostErrorCode.FORBIDDEN);
    }
    eventPostRepository.delete(post);
  }

  private EventPost findPostOrThrow(Long postId) {
    return eventPostRepository
        .findById(postId)
        .orElseThrow(() -> new EventPostException(EventPostErrorCode.POST_NOT_FOUND));
  }

  private void saveImages(EventPost post, List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) return;
    for (int i = 0; i < imageUrls.size(); i++) {
      eventPostImageRepository.save(EventPostImage.of(post, imageUrls.get(i), i));
    }
  }
}
