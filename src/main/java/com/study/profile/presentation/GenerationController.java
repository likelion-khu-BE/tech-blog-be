package com.study.profile.presentation;

import com.study.profile.application.GenerationService;
import com.study.profile.application.dto.GenerationCreateRequest;
import com.study.profile.application.dto.GenerationDto;
import com.study.profile.application.dto.GenerationMemberAddRequest;
import com.study.profile.application.dto.GenerationMemberDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/generations")
public class GenerationController {

  private final GenerationService generationService;

  public GenerationController(GenerationService generationService) {
    this.generationService = generationService;
  }

  @GetMapping
  public ResponseEntity<List<GenerationDto>> getGenerations() {
    return ResponseEntity.ok(generationService.getGenerations());
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Integer>> createGeneration(
      @Valid @RequestBody GenerationCreateRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(generationService.createGeneration(req));
  }

  @GetMapping("/{generationId}")
  public ResponseEntity<GenerationDto> getGeneration(@PathVariable Integer generationId) {
    return ResponseEntity.ok(generationService.getGeneration(generationId));
  }

  @PatchMapping("/{generationId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Integer>> updateGeneration(
      @PathVariable Integer generationId, @Valid @RequestBody GenerationCreateRequest req) {
    return ResponseEntity.ok(generationService.updateGeneration(generationId, req));
  }

  @GetMapping("/{generationId}/members")
  public ResponseEntity<List<GenerationMemberDto>> getGenerationMembers(
      @PathVariable Integer generationId) {
    return ResponseEntity.ok(generationService.getGenerationMembers(generationId));
  }

  @PostMapping("/{generationId}/members")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Integer>> addMemberToGeneration(
      @PathVariable Integer generationId, @Valid @RequestBody GenerationMemberAddRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(generationService.addMemberToGeneration(generationId, req));
  }
}
