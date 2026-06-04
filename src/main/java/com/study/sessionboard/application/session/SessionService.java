package com.study.sessionboard.application.session;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.sessionboard.application.session.exception.SessionNotFoundException;
import com.study.sessionboard.domain.session.*;
import com.study.sessionboard.infrastructure.session.ResourceRepository;
import com.study.sessionboard.infrastructure.session.RetroRepository;
import com.study.sessionboard.infrastructure.session.SessionNoteRepository;
import com.study.sessionboard.infrastructure.session.SessionRepository;
import com.study.sessionboard.infrastructure.session.SessionSpeakerRepository;
import com.study.sessionboard.presentation.dto.session.*;
import com.study.shared.extevent.sessionboard.SessionSpeakerRegistered;
import com.study.shared.extevent.sessionboard.SessionSpeakerUnregistered;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {

  private final SessionRepository sessionRepository;
  private final SessionSpeakerRepository sessionSpeakerRepository;
  private final SessionNoteRepository sessionNoteRepository;
  private final ResourceRepository resourceRepository;
  private final RetroRepository retroRepository;
  private final GenerationRepository generationRepository;
  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;

  public List<SessionResponse> getSessions(Integer generationNumber, SessionStatus status) {
    if (!generationRepository.existsById(generationNumber)) {
      throw new IllegalArgumentException("존재하지 않는 기수 번호입니다.");
    }

    List<Session> sessions =
        (status == null)
            ? sessionRepository.findAllByGenerationNumber(generationNumber)
            : sessionRepository.findAllByGenerationNumberAndStatus(generationNumber, status);

    return sessions.stream().map(this::toResponse).toList();
  }

  public SessionResponse getSession(Integer generationNumber, Long sessionId) {
    Session session =
        sessionRepository
            .findByGenerationNumberAndId(generationNumber, sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    return toResponse(session);
  }

  @Transactional
  public Long createSession(Integer generationNumber, SessionCreateRequest request) {
    Generation generation =
        generationRepository
            .findById(generationNumber)
            .orElseThrow(() -> new IllegalArgumentException("해당 기수를 찾을 수 없습니다."));

    Session session =
        Session.create(
            generation,
            request.weekLabel(),
            request.title(),
            request.status(),
            request.startedAt());

    Session savedSession = sessionRepository.save(session);

    if (request.speakerIds() != null && !request.speakerIds().isEmpty()) {
      List<Member> speakers = memberRepository.findAllById(request.speakerIds());

      if (speakers.size() != request.speakerIds().size()) {
        throw new IllegalArgumentException("존재하지 않는 스피커 ID가 포함되어 있습니다.");
      }

      speakers.forEach(
          member -> {
            savedSession.addSpeaker(SessionSpeaker.of(savedSession, member));
            eventPublisher.publishEvent(
                new SessionSpeakerRegistered(member.getUser().getId(), savedSession.getId()));
          });
    }

    return savedSession.getId();
  }

  @Transactional
  public OffsetDateTime updateSession(
      Integer generationNumber, Long sessionId, SessionUpdateRequest request) {
    Session session =
        sessionRepository
            .findByGenerationNumberAndId(generationNumber, sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

    session.update(request.weekLabel(), request.title(), request.status(), request.startedAt());

    if (request.speakerIds() != null) {
      Set<Long> existingUserIds =
          session.getSpeakers().stream()
              .map(s -> s.getMember().getUser().getId())
              .collect(Collectors.toSet());

      session.getSpeakers().clear();
      List<Member> speakers = memberRepository.findAllById(request.speakerIds());

      if (speakers.size() != request.speakerIds().size()) {
        throw new IllegalArgumentException("존재하지 않는 스피커 ID가 포함되어 있습니다.");
      }

      Set<Long> newUserIds =
          speakers.stream().map(m -> m.getUser().getId()).collect(Collectors.toSet());

      speakers.forEach(member -> session.addSpeaker(SessionSpeaker.of(session, member)));

      existingUserIds.stream()
          .filter(id -> !newUserIds.contains(id))
          .forEach(
              uid ->
                  eventPublisher.publishEvent(new SessionSpeakerUnregistered(uid, sessionId)));

      newUserIds.stream()
          .filter(id -> !existingUserIds.contains(id))
          .forEach(
              uid ->
                  eventPublisher.publishEvent(new SessionSpeakerRegistered(uid, sessionId)));
    }

    return OffsetDateTime.now(); // Updated at is not in the entity, using current time for response
  }

  @Transactional
  public void deleteSession(Integer generationNumber, Long sessionId) {
    Session session =
        sessionRepository
            .findByGenerationNumberAndId(generationNumber, sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
    sessionRepository.delete(session);
  }

  private SessionResponse toResponse(Session session) {
    List<SessionSpeaker> speakers = sessionSpeakerRepository.findAllBySession(session);
    Double averageRating = retroRepository.getAverageRatingBySession(session);
    long noteCount = sessionNoteRepository.countBySession(session);
    long resourceCount = resourceRepository.countBySession(session);

    return SessionResponse.of(session, speakers, averageRating, noteCount, resourceCount);
  }

  public List<ResourceResponse> getResources(Long sessionId, String type) {
    Session session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

    List<Resource> resources =
        (type == null)
            ? resourceRepository.findAllBySession(session)
            : resourceRepository.findAllBySessionAndType(session, type);

    return resources.stream().map(ResourceResponse::from).toList();
  }

  @Transactional
  public ResourceResponse createResource(
      Long sessionId, Long userId, ResourceCreateRequest request) {
    Session session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

    Member uploader = memberRepository.getReferenceById(userId);

    Resource resource =
        Resource.of(session, uploader, request.type(), request.name(), request.url());

    return ResourceResponse.from(resourceRepository.save(resource));
  }

  @Transactional
  public void deleteResource(Long resourceId, Long userId) {
    Resource resource =
        resourceRepository
            .findById(resourceId)
            .orElseThrow(() -> new IllegalArgumentException("해당 자료를 찾을 수 없습니다."));

    if (!resource.getUploader().getId().equals(userId)) {
      throw new IllegalStateException("본인이 업로드한 자료만 삭제할 수 있습니다.");
    }

    resourceRepository.delete(resource);
  }

  public List<RetroResponse> getRetros(Long sessionId) {
    Session session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

    return retroRepository.findAllBySession(session).stream().map(RetroResponse::from).toList();
  }

  @Transactional
  public RetroResponse createRetro(Long sessionId, Long userId, RetroCreateRequest request) {
    Session session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));

    if (retroRepository.existsBySessionAndAuthorId(session, userId)) {
      throw new IllegalStateException("이미 회고를 작성했습니다.");
    }

    Member author = memberRepository.getReferenceById(userId);
    Retro retro = Retro.of(session, author, request.rating(), request.body());

    return RetroResponse.from(retroRepository.save(retro));
  }

  @Transactional
  public RetroResponse updateRetro(Long retroId, Long userId, RetroCreateRequest request) {
    Retro retro =
        retroRepository
            .findById(retroId)
            .orElseThrow(() -> new IllegalArgumentException("해당 회고를 찾을 수 없습니다."));

    if (!retro.getAuthor().getId().equals(userId)) {
      throw new IllegalStateException("본인 회고만 수정할 수 있습니다.");
    }

    retro.update(request.rating(), request.body());
    return RetroResponse.from(retro);
  }
}
