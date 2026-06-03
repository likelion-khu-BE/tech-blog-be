package com.study.sessionboard.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.study.profile.domain.member.Member;
import com.study.sessionboard.application.event.dto.EventPostSummaryResponse;
import com.study.sessionboard.application.event.dto.PageWrapper;
import com.study.sessionboard.domain.event.EventPost;
import com.study.sessionboard.domain.event.EventPostStatus;
import com.study.sessionboard.domain.event.EventPostType;
import com.study.sessionboard.infrastructure.event.EventPostCommentRepository;
import com.study.sessionboard.infrastructure.event.EventPostImageRepository;
import com.study.sessionboard.infrastructure.event.EventPostRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class EventPostServiceTest {

  @Mock EventPostRepository eventPostRepository;
  @Mock EventPostImageRepository eventPostImageRepository;
  @Mock EventPostCommentRepository eventPostCommentRepository;

  @InjectMocks EventPostService eventPostService;

  // ── 테스트 1: PUBLISHED 게시글만 반환 ──────────────────────────────────────

  @Test
  void getEventPosts_returnsOnlyPublishedPosts() {
    Integer generationId = 1;
    Pageable pageable = PageRequest.of(0, 20);

    Member mockMember = org.mockito.Mockito.mock(Member.class);
    given(mockMember.getId()).willReturn(1L);
    given(mockMember.getName()).willReturn("홍길동");

    EventPost post = org.mockito.Mockito.mock(EventPost.class);
    given(post.getId()).willReturn(10L);
    given(post.getTitle()).willReturn("테스트 이벤트 게시글");
    given(post.getType()).willReturn(EventPostType.MEETUP);
    given(post.getAuthor()).willReturn(mockMember);
    given(post.getBody()).willReturn("본문 내용입니다.");
    given(post.getTags()).willReturn(new String[]{});
    given(post.getLikeCount()).willReturn(0);
    given(post.getCreatedAt()).willReturn(null);

    given(eventPostRepository.findAllWithFilters(
            eq(generationId), eq(EventPostStatus.PUBLISHED), eq(null), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(post), pageable, 1));
    given(eventPostImageRepository.findFirstImagesByPostIdIn(List.of(10L)))
        .willReturn(List.of());
    given(eventPostCommentRepository.countByPostIdIn(List.of(10L)))
        .willReturn(List.of());

    PageWrapper<EventPostSummaryResponse> result =
        eventPostService.getEventPosts(generationId, null, pageable);

    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).title()).isEqualTo("테스트 이벤트 게시글");
  }

  // ── 테스트 2: type 필터가 repository에 그대로 전달된다 ────────────────────────

  @Test
  void getEventPosts_withTypeFilter_passesTypeToRepository() {
    Integer generationId = 1;
    Pageable pageable = PageRequest.of(0, 20);
    EventPostType filterType = EventPostType.HACKATHON;

    given(eventPostRepository.findAllWithFilters(
            eq(generationId), eq(EventPostStatus.PUBLISHED), eq(filterType), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(), pageable, 0));

    eventPostService.getEventPosts(generationId, filterType, pageable);

    verify(eventPostRepository)
        .findAllWithFilters(generationId, EventPostStatus.PUBLISHED, filterType, pageable);
  }

  // ── 테스트 3: 썸네일 이미지가 있으면 thumbUrl이 응답에 포함된다 ──────────────────

  @Test
  void getEventPosts_withThumbnail_includesThumbUrl() {
    Integer generationId = 1;
    Pageable pageable = PageRequest.of(0, 20);
    String expectedUrl = "https://example.com/thumb.jpg";

    Member mockMember = org.mockito.Mockito.mock(Member.class);
    given(mockMember.getId()).willReturn(1L);
    given(mockMember.getName()).willReturn("홍길동");

    EventPost post = org.mockito.Mockito.mock(EventPost.class);
    given(post.getId()).willReturn(10L);
    given(post.getTitle()).willReturn("썸네일 있는 게시글");
    given(post.getType()).willReturn(EventPostType.WORKSHOP);
    given(post.getAuthor()).willReturn(mockMember);
    given(post.getBody()).willReturn("본문");
    given(post.getTags()).willReturn(new String[]{});
    given(post.getLikeCount()).willReturn(0);
    given(post.getCreatedAt()).willReturn(null);

    com.study.sessionboard.domain.event.EventPostImage image =
        org.mockito.Mockito.mock(com.study.sessionboard.domain.event.EventPostImage.class);
    given(image.getPost()).willReturn(post);
    given(image.getUrl()).willReturn(expectedUrl);

    given(eventPostRepository.findAllWithFilters(
            eq(generationId), eq(EventPostStatus.PUBLISHED), eq(null), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(post), pageable, 1));
    given(eventPostImageRepository.findFirstImagesByPostIdIn(List.of(10L)))
        .willReturn(List.of(image));
    given(eventPostCommentRepository.countByPostIdIn(List.of(10L)))
        .willReturn(List.of());

    PageWrapper<EventPostSummaryResponse> result =
        eventPostService.getEventPosts(generationId, null, pageable);

    assertThat(result.content().get(0).thumbUrl()).isEqualTo(expectedUrl);
    assertThat(result.content().get(0).hasThumb()).isTrue();
  }

  // ── 테스트 4: 댓글 수가 게시글별로 올바르게 매핑된다 ─────────────────────────────

  @Test
  void getEventPosts_withComments_mapsCommentCountPerPost() {
    Integer generationId = 1;
    Pageable pageable = PageRequest.of(0, 20);

    Member mockMember = org.mockito.Mockito.mock(Member.class);
    given(mockMember.getId()).willReturn(1L);
    given(mockMember.getName()).willReturn("홍길동");

    EventPost postA = org.mockito.Mockito.mock(EventPost.class);
    given(postA.getId()).willReturn(10L);
    given(postA.getTitle()).willReturn("게시글 A");
    given(postA.getType()).willReturn(EventPostType.PROJECT);
    given(postA.getAuthor()).willReturn(mockMember);
    given(postA.getBody()).willReturn("본문");
    given(postA.getTags()).willReturn(new String[]{});
    given(postA.getLikeCount()).willReturn(0);
    given(postA.getCreatedAt()).willReturn(null);

    EventPost postB = org.mockito.Mockito.mock(EventPost.class);
    given(postB.getId()).willReturn(20L);
    given(postB.getTitle()).willReturn("게시글 B");
    given(postB.getType()).willReturn(EventPostType.PROJECT);
    given(postB.getAuthor()).willReturn(mockMember);
    given(postB.getBody()).willReturn("본문");
    given(postB.getTags()).willReturn(new String[]{});
    given(postB.getLikeCount()).willReturn(0);
    given(postB.getCreatedAt()).willReturn(null);

    given(eventPostRepository.findAllWithFilters(
            eq(generationId), eq(EventPostStatus.PUBLISHED), eq(null), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(postA, postB), pageable, 2));
    given(eventPostImageRepository.findFirstImagesByPostIdIn(List.of(10L, 20L)))
        .willReturn(List.of());
    given(eventPostCommentRepository.countByPostIdIn(List.of(10L, 20L)))
        .willReturn(List.of(new Object[]{10L, 3L}, new Object[]{20L, 7L}));

    PageWrapper<EventPostSummaryResponse> result =
        eventPostService.getEventPosts(generationId, null, pageable);

    assertThat(result.content().get(0).commentCount()).isEqualTo(3);
    assertThat(result.content().get(1).commentCount()).isEqualTo(7);
  }

  // ── 테스트 5: 게시글이 없으면 빈 페이지가 반환된다 ───────────────────────────────

  @Test
  void getEventPosts_noPosts_returnsEmptyPage() {
    Integer generationId = 1;
    Pageable pageable = PageRequest.of(0, 20);

    given(eventPostRepository.findAllWithFilters(
            eq(generationId), eq(EventPostStatus.PUBLISHED), eq(null), eq(pageable)))
        .willReturn(new PageImpl<>(List.of(), pageable, 0));

    PageWrapper<EventPostSummaryResponse> result =
        eventPostService.getEventPosts(generationId, null, pageable);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.hasNext()).isFalse();
  }
}
