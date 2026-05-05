package com.study.sessionboard.application.event;

import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import com.study.sessionboard.domain.event.EventPostType;
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

  public EventPostService(
      EventPostRepository eventPostRepository,
      EventPostImageRepository eventPostImageRepository) {
    this.eventPostRepository = eventPostRepository;
    this.eventPostImageRepository = eventPostImageRepository;
  }

  public PageWrapper<EventPostSummaryResponse> getEventPosts(
      Long generationId, EventPostType type, Pageable pageable) {

    Page<EventPost> posts =
        eventPostRepository.findAllWithFilters(
            generationId, EventPostStatus.PUBLISHED, type, pageable);

    List<Long> postIds = posts.map(EventPost::getId).toList();

    // 썸네일 URL: 게시글당 첫 번째 이미지만 배치 조회
    Map<Long, String> thumbMap = new HashMap<>();
    if (!postIds.isEmpty()) {
      eventPostImageRepository
          .findFirstImagesByPostIdIn(postIds)
          .forEach(img -> thumbMap.put(img.getPost().getId(), img.getUrl()));
    }

    Page<EventPostSummaryResponse> responsePage =
        posts.map(post -> EventPostSummaryResponse.of(post, thumbMap.get(post.getId())));

    return PageWrapper.from(responsePage);
  }
}
