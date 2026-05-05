package com.study.sessionboard.application;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostImage;
import com.study.sessionboard.infrastructure.EventPostImageRepository;
import com.study.sessionboard.infrastructure.EventPostRepository;
import com.study.sessionboard.presentation.dto.EventPostCreateRequest;
import com.study.sessionboard.presentation.dto.EventPostResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPostService {

  private final EventPostRepository eventPostRepository;
  private final EventPostImageRepository eventPostImageRepository;
  private final EntityManager entityManager;

  public EventPostResponse getEventPost(Long eventPostId) {
    EventPost post =
        eventPostRepository
            .findById(eventPostId)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

    List<EventPostImage> images =
        eventPostImageRepository.findAllByPostIdOrderByOrderAsc(eventPostId);

    return EventPostResponse.builder()
        .id(post.getId())
        .type(post.getType())
        .status(post.getStatus())
        .title(post.getTitle())
        .author(
            EventPostResponse.AuthorResponse.builder()
                .id(post.getAuthor().getId())
                .name(post.getAuthor().getName())
                .initial(
                    post.getAuthor()
                        .getName()
                        .substring(0, Math.min(post.getAuthor().getName().length(), 2)))
                .build())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .excerpt(post.getExcerpt())
        .body(post.getBody())
        .tags(List.of(post.getTags()))
        .images(
            images.stream()
                .map(
                    img ->
                        EventPostResponse.ImageResponse.builder()
                            .order(img.getOrder())
                            .url(img.getUrl())
                            .build())
                .collect(Collectors.toList()))
        .likeCount(post.getLikeCount())
        .likedByMe(false)
        .commentCount(post.getCommentCount())
        .build();
  }

  @Transactional
  public Long createEventPost(Long userId, Long generationId, EventPostCreateRequest request) {
    Member author =
        entityManager
            .createQuery("SELECT m FROM Member m WHERE m.user.id = :userId", Member.class)
            .setParameter("userId", userId)
            .getSingleResult();

    Generation generation = entityManager.find(Generation.class, generationId);
    if (generation == null) {
      throw new IllegalArgumentException("해당 기수를 찾을 수 없습니다.");
    }

    EventPost post =
        EventPost.of(
            author,
            generation,
            request.getType(),
            request.getTitle(),
            request.getBody(),
            request.getTags() != null ? request.getTags().toArray(new String[0]) : null);

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
        request.getType(),
        request.getTitle(),
        request.getBody(),
        request.getTags() != null ? request.getTags().toArray(new String[0]) : null);
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
