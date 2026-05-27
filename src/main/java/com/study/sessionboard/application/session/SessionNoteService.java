package com.study.sessionboard.application.session;

import com.study.profile.domain.exception.MemberNotFoundException;
import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import com.study.sessionboard.application.session.exception.SessionNotFoundException;
import com.study.sessionboard.domain.session.NoteLink;
import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionNote;
import com.study.sessionboard.infrastructure.session.SessionNoteRepository;
import com.study.sessionboard.infrastructure.session.SessionRepository;
import com.study.sessionboard.presentation.dto.session.SessionNoteRequest;
import com.study.sessionboard.presentation.dto.session.SessionNoteResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionNoteService {

  private final SessionNoteRepository sessionNoteRepository;
  private final SessionRepository sessionRepository;
  private final MemberRepository memberRepository;

  public List<SessionNoteResponse> getNotes(
      Integer generationNumber, Long sessionId, String query) {
    validateSession(generationNumber, sessionId);
    return sessionNoteRepository.findAllBySessionIdAndQuery(sessionId, query).stream()
        .map(SessionNoteResponse::from)
        .toList();
  }

  @Transactional
  public SessionNoteResponse createNote(
      Integer generationNumber, Long sessionId, SessionNoteRequest request, Long userId) {
    Session session = validateSession(generationNumber, sessionId);

    Member author =
        memberRepository
            .findByUserId(userId)
            .orElseThrow(() -> new MemberNotFoundException(userId));

    SessionNote note = SessionNote.of(session, author, request.body());

    if (request.links() != null) {
      request
          .links()
          .forEach(
              linkReq -> {
                note.addLink(NoteLink.of(note, linkReq.label(), linkReq.url(), linkReq.order()));
              });
    }

    return SessionNoteResponse.from(sessionNoteRepository.save(note));
  }

  @Transactional
  public SessionNoteResponse updateNote(
      Integer generationNumber, Long sessionId, Long noteId, SessionNoteRequest request) {
    validateSession(generationNumber, sessionId);

    SessionNote note =
        sessionNoteRepository
            .findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다."));

    if (!note.getSession().getId().equals(sessionId)) {
      throw new IllegalArgumentException("해당 세션의 노트가 아닙니다.");
    }

    note.update(request.body());
    note.clearLinks();

    if (request.links() != null) {
      request
          .links()
          .forEach(
              linkReq -> {
                note.addLink(NoteLink.of(note, linkReq.label(), linkReq.url(), linkReq.order()));
              });
    }

    return SessionNoteResponse.from(note);
  }

  @Transactional
  public void deleteNote(Integer generationNumber, Long sessionId, Long noteId) {
    validateSession(generationNumber, sessionId);

    SessionNote note =
        sessionNoteRepository
            .findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("해당 노트를 찾을 수 없습니다."));

    if (!note.getSession().getId().equals(sessionId)) {
      throw new IllegalArgumentException("해당 세션의 노트가 아닙니다.");
    }

    sessionNoteRepository.delete(note);
  }

  private Session validateSession(Integer generationNumber, Long sessionId) {
    return sessionRepository
        .findByGenerationNumberAndId(generationNumber, sessionId)
        .orElseThrow(() -> new SessionNotFoundException(sessionId));
  }
}
