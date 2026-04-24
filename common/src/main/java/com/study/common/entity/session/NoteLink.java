package com.study.common.entity.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "note_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "note_id", nullable = false)
  private SessionNote note;

  private String label;

  @Column(nullable = false)
  private String url;

  @Column(name = "\"order\"", nullable = false)
  private int order = 0;

  public static NoteLink of(SessionNote note, String url) {
    NoteLink link = new NoteLink();
    link.note = note;
    link.url = url;
    return link;
  }
}
