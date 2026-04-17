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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팀 이미지 엔티티.
 *
 * <p>팀 프로필에 첨부된 이미지 URL을 저장한다. 한 팀(TeamProfile)에 이미지가 0개 ~ 여러 개 있을 수 있으므로 별도 테이블로 분리했다. 이미지를 추가/교체할
 * 때는 TeamProfile.updateImages()를 통해 관리한다.
 *
 * <p>[DB 테이블: team_image]
 */
@Entity
@Table(name = "team_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id; // DB가 자동으로 부여하는 고유 번호

  // 어떤 팀의 이미지인지 (team_id 외래키로 TeamProfile 테이블 참조)
  // ON DELETE CASCADE : 팀이 삭제되면 이미지 행도 자동으로 삭제된다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "team_id")
  private TeamProfile team;

  @Column(name = "image_url", nullable = false)
  private String imageUrl; // 이미지 URL (S3, CDN 등 외부 저장소 경로)

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt; // 이미지 등록 시각

  /** 팀에 이미지를 등록할 때 사용하는 정적 팩토리 메서드. TeamProfile.updateImages() 내부에서 URL 수만큼 반복 호출된다. */
  public static TeamImage create(TeamProfile team, String imageUrl) {
    TeamImage image = new TeamImage();
    image.team = team;
    image.imageUrl = imageUrl;
    return image;
  }

  /** DB 저장 직전에 JPA가 자동으로 현재 시각을 createdAt에 넣어준다. */
  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }
}
