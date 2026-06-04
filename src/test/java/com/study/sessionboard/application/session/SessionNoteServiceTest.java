package com.study.sessionboard.application.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.study.profile.domain.member.Member;
import com.study.profile.infrastructure.MemberRepository;
import com.study.sessionboard.domain.session.Session;
import com.study.sessionboard.domain.session.SessionNote;
import com.study.sessionboard.infrastructure.session.SessionNoteRepository;
import com.study.sessionboard.infrastructure.session.SessionRepository;
import com.study.sessionboard.presentation.dto.session.SessionNoteRequest;
import com.study.sessionboard.presentation.dto.session.SessionNoteResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionNoteServiceTest {

  @Mock private SessionNoteRepository sessionNoteRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private MemberRepository memberRepository;

  @InjectMocks private SessionNoteService sessionNoteService;

  private Session testSession;
  private Member testMember;

  @BeforeEach
  void setUp() {
    testSession = mock(Session.class);
    testMember = mock(Member.class);

    given(testSession.getId()).willReturn(1L);
    given(testMember.getId()).willReturn(1L);
    given(testMember.getName()).willReturn("테스터");
  }

  @Test
  @DisplayName("노트 작성 성공")
  void createNote_Success() {
    // given
    Integer generationNumber = 13;
    Long sessionId = 1L;
    SessionNoteRequest request = new SessionNoteRequest("본문", List.of());

    given(sessionRepository.findByGenerationNumberAndId(generationNumber, sessionId))
        .willReturn(Optional.of(testSession));
    given(memberRepository.findByUserId(1L)).willReturn(Optional.of(testMember));

    SessionNote note = SessionNote.of(testSession, testMember, request.body());
    given(sessionNoteRepository.save(any(SessionNote.class))).willReturn(note);

    // when
    SessionNoteResponse response =
        sessionNoteService.createNote(generationNumber, sessionId, request, 1L);

    // then
    assertThat(response.body()).isEqualTo("본문");
    assertThat(response.author().id()).isEqualTo(1L);
    verify(sessionNoteRepository).save(any(SessionNote.class));
  }

  @Test
  @DisplayName("노트 수정 성공")
  void updateNote_Success() {
    // given
    Integer generationNumber = 13;
    Long sessionId = 1L;
    Long noteId = 100L;
    SessionNoteRequest request = new SessionNoteRequest("수정된 본문", List.of());

    SessionNote existingNote = SessionNote.of(testSession, testMember, "기존 본문");

    given(sessionRepository.findByGenerationNumberAndId(generationNumber, sessionId))
        .willReturn(Optional.of(testSession));
    given(sessionNoteRepository.findById(noteId)).willReturn(Optional.of(existingNote));

    // when
    SessionNoteResponse response =
        sessionNoteService.updateNote(generationNumber, sessionId, noteId, request);

    // then
    assertThat(response.body()).isEqualTo("수정된 본문");
    assertThat(existingNote.getBody()).isEqualTo("수정된 본문");
  }
}
