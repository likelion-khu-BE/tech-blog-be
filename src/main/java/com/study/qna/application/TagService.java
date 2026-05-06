package com.study.qna.application;

import com.study.qna.application.dto.request.tag.TagCreateRequest;
import com.study.qna.application.dto.response.tag.TagResponse;
import com.study.qna.domain.Tag;
import com.study.qna.domain.exception.TagAlreadyExistsException;
import com.study.qna.domain.exception.TagNotFoundException;
import com.study.qna.infrastructure.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

  private final TagRepository tagRepository;

  public List<TagResponse> getTags() {
    return tagRepository.findAllByOrderByNameAsc().stream().map(TagResponse::from).toList();
  }

  @Transactional
  public void deleteTag(Long tagId) {
    Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new TagNotFoundException(tagId));
    tagRepository.delete(tag);
  }

  @Transactional
  public TagResponse createTag(TagCreateRequest request) {
    if (tagRepository.existsByName(request.name())) {
      throw new TagAlreadyExistsException(request.name());
    }
    Tag saved = tagRepository.save(Tag.create(request.name()));
    return TagResponse.from(saved);
  }
}