package com.study.sessionboard.application.session;

import com.study.profile.domain.generation.Generation;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.GenerationRepository;
import com.study.profile.infrastructure.MemberRepository;
import com.study.sessionboard.application.session.exception.SessionNotFoundException;
import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionSpeaker;
import com.study.sessionboard.domain.session.SessionStatus;
import com.study.sessionboard.infrastructure.session.ResourceRepository;
import com.study.sessionboard.infrastructure.session.RetroRepository;
import com.study.sessionboard.infrastructure.session.SessionNoteRepository;
import com.study.sessionboard.infrastructure.session.SessionRepository;
import com.study.sessionboard.infrastructure.session.SessionSpeakerRepository;
import com.study.sessionboard.presentation.dto.session.SessionCreateRequest;
import com.study.sessionboard.presentation.dto.session.SessionResponse;
import com.study.sessionboard.presentation.dto.session.SessionUpdateRequest;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

  public List<SessionResponse> getSessions(Integer generationNumber, SessionStatus status) {
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
      session.getSpeakers().clear();
      List<Member> speakers = memberRepository.findAllById(request.speakerIds());

      if (speakers.size() != request.speakerIds().size()) {
        throw new IllegalArgumentException("존재하지 않는 스피커 ID가 포함되어 있습니다.");
      }

      speakers.forEach(
          member -> {
            session.addSpeaker(SessionSpeaker.of(session, member));
          });
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
}
