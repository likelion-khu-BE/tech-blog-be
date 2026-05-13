package com.study.sessionboard.application.event;

import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.infrastructure.event.EventPostCommentRepository;
import com.study.sessionboard.infrastructure.event.EventPostImageRepository;
import com.study.sessionboard.infrastructure.event.EventPostRepository;
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
  private final EventPostCommentRepository eventPostCommentRepository;

  public EventPostService(
      EventPostRepository eventPostRepository,
      EventPostImageRepository eventPostImageRepository,
      EventPostCommentRepository eventPostCommentRepository) {
    this.eventPostRepository = eventPostRepository;
    this.eventPostImageRepository = eventPostImageRepository;
    this.eventPostCommentRepository = eventPostCommentRepository;
  }

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
}