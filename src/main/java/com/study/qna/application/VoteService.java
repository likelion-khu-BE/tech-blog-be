package com.study.qna.application;

import com.study.qna.application.dto.request.vote.VoteCreateRequest;
import com.study.qna.application.dto.response.vote.MyVoteResponse;
import com.study.qna.domain.Answer;
import com.study.qna.domain.Vote;
import com.study.qna.domain.VoteType;
import com.study.qna.domain.exception.AnswerNotFoundException;
import com.study.qna.domain.exception.VoteAlreadyExistsException;
import com.study.qna.domain.exception.VoteNotFoundException;
import com.study.qna.domain.exception.VoteSelfNotAllowedException;
import com.study.qna.infrastructure.AnswerRepository;
import com.study.qna.infrastructure.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

  private final VoteRepository voteRepository;
  private final AnswerRepository answerRepository;

  @Transactional
  public void createVote(Long answerId, VoteCreateRequest request, Long userId) {
    Answer answer =
        answerRepository
            .findById(answerId)
            .orElseThrow(() -> new AnswerNotFoundException(answerId));

    if (answer.isAuthor(userId)) {
      throw new VoteSelfNotAllowedException();
    }

    if (voteRepository.findByAnswer_IdAndUserId(answerId, userId).isPresent()) {
      throw new VoteAlreadyExistsException();
    }

    VoteType type = VoteType.valueOf(request.type());
    voteRepository.save(Vote.create(answer, userId, type));
    answerRepository.updateVoteCount(answerId, type == VoteType.UPVOTE ? 1 : -1);
  }

  @Transactional
  public void cancelVote(Long answerId, Long userId) {
    if (!answerRepository.existsById(answerId)) throw new AnswerNotFoundException(answerId);

    Vote vote =
        voteRepository
            .findByAnswer_IdAndUserId(answerId, userId)
            .orElseThrow(VoteNotFoundException::new);

    VoteType type = vote.getType();
    voteRepository.delete(vote);
    answerRepository.updateVoteCount(answerId, type == VoteType.UPVOTE ? -1 : 1);
  }

  public MyVoteResponse getMyVote(Long answerId, Long userId) {
    if (!answerRepository.existsById(answerId)) throw new AnswerNotFoundException(answerId);

    return voteRepository
        .findByAnswer_IdAndUserId(answerId, userId)
        .map(MyVoteResponse::of)
        .orElse(MyVoteResponse.noVote());
  }
}
