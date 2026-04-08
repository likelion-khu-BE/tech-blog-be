package com.study.contract.profile;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface ProfilePort {

  UserInfo getUser(UUID userId);

  GenerationInfo getGeneration(Integer generationId);

  @Getter
  @NoArgsConstructor
  class UserInfo {

    private UUID id;
    private String name;
    private String initial;
    private String avatarStyle;

    public UserInfo(UUID id, String name, String initial, String avatarStyle) {
      this.id = id;
      this.name = name;
      this.initial = initial;
      this.avatarStyle = avatarStyle;
    }
  }

  @Getter
  @NoArgsConstructor
  class GenerationInfo {

    private Integer id;
    private String label;
    private boolean isCurrent;

    public GenerationInfo(Integer id, String label, boolean isCurrent) {
      this.id = id;
      this.label = label;
      this.isCurrent = isCurrent;
    }
  }
}
