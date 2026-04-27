package com.study.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resource")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @Column(name = "uploader_id", nullable = false)
  private UUID uploaderId;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String url;

  @Column(name = "size_label")
  private String sizeLabel;

  @Column(nullable = false)
  private ResourceVisibility visibility = ResourceVisibility.MEMBER;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private OffsetDateTime uploadedAt;

  @PrePersist
  void prePersist() {
    uploadedAt = OffsetDateTime.now();
  }

  public static Resource of(Session session, UUID uploaderId, String type, String name, String url) {
    Resource resource = new Resource();
    resource.session = session;
    resource.uploaderId = uploaderId;
    resource.type = type;
    resource.name = name;
    resource.url = url;
    return resource;
  }
}
