package com.study.qna.presentation;

import com.study.qna.application.TagService;
import com.study.qna.application.dto.request.tag.TagCreateRequest;
import com.study.qna.application.dto.response.tag.TagResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {

  private final TagService tagService;

  @GetMapping
  public ResponseEntity<List<TagResponse>> getTags() {
    return ResponseEntity.ok(tagService.getTags());
  }

  @PostMapping
  public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(request));
  }

  @DeleteMapping("/{tagId}")
  public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
    tagService.deleteTag(tagId);
    return ResponseEntity.noContent().build();
  }
}
