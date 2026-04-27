package com.study.sessionboard.service;

import com.study.common.entity.EventPost;
import com.study.sessionboard.dto.EventPostRequest;
import com.study.sessionboard.dto.EventPostResponse;
import com.study.sessionboard.repository.EventPostRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventPostService {

  private final EventPostRepository postRepository;

  // C: 게시글 생성
  public EventPostResponse create(EventPostRequest request) {
    EventPost post =
        EventPost.of(
            request.getAuthorId(),
            request.getGenerationId(),
            request.getType(),
            request.getTitle());
    return EventPostResponse.from(postRepository.save(post));
  }

  // R: 기수별 목록
  @Transactional(readOnly = true)
  public List<EventPostResponse> getByGeneration(Integer generationId) {
    return postRepository.findByGenerationIdOrderByPublishedAtDesc(generationId).stream()
        .map(EventPostResponse::from)
        .toList();
  }

  // R: 게시글 하나
  @Transactional(readOnly = true)
  public EventPostResponse getOne(UUID postId) {
    EventPost post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없어요"));
    return EventPostResponse.from(post);
  }

  // R: 내가 쓴 게시글
  @Transactional(readOnly = true)
  public List<EventPostResponse> getMyPosts(UUID authorId) {
    return postRepository.findByAuthorIdOrderByPublishedAtDesc(authorId).stream()
        .map(EventPostResponse::from)
        .toList();
  }

  // U: 수정
  public EventPostResponse update(UUID postId, EventPostRequest request) {
    EventPost post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없어요"));

    if (!post.getAuthorId().equals(request.getAuthorId())) {
      throw new IllegalArgumentException("본인 게시글만 수정할 수 있어요");
    }

    return EventPostResponse.from(post);
  }

  // D: 삭제
  public void delete(UUID postId, UUID requesterId) {
    EventPost post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없어요"));

    if (!post.getAuthorId().equals(requesterId)) {
      throw new IllegalArgumentException("본인 게시글만 삭제할 수 있어요");
    }

    postRepository.delete(post);
  }
}
