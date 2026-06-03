package com.study.sessionboard.domain.session;

import com.study.profile.domain.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "session_note")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionNote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private Session session;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private Member author;

  @Column(columnDefinition = "TEXT")
  private String body;

  @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<NoteLink> links = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public static SessionNote of(Session session, Member author, String body) {
    SessionNote note = new SessionNote();
    note.session = session;
    note.author = author;
    note.body = body;
    return note;
  }

  public void update(String body) {
    this.body = body;
  }

  public void addLink(NoteLink link) {
    this.links.add(link);
  }

  public void clearLinks() {
    this.links.clear();
  }
}
