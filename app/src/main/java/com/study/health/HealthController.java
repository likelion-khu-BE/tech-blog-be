package com.study.health;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 헬스체크 엔드포인트.
 *
 * <p>liveness와 readiness를 분리한다. liveness에 DB 체크를 넣으면 DB 장애 시 컨테이너 재시작이 반복되며 장애가 증폭되기 때문에, liveness는
 * JVM 생존만 확인한다.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

  private final DataSource dataSource;

  /** JVM이 살아있는지만 확인한다. DB 등 외부 의존성 체크하지 않는다. */
  @GetMapping
  public Map<String, String> liveness() {
    return Map.of("status", "UP");
  }

  /** 트래픽을 받을 준비가 됐는지 확인한다. DB 커넥션이 유효하지 않으면 503을 반환해 로드밸런서/배포 파이프라인이 해당 인스턴스를 제외하도록 한다. */
  @GetMapping("/ready")
  public ResponseEntity<Map<String, String>> readiness() {
    try (Connection conn = dataSource.getConnection()) {
      if (conn.isValid(1)) {
        return ResponseEntity.ok(Map.of("status", "UP"));
      }
      return ResponseEntity.status(503).body(Map.of("status", "DOWN", "reason", "db invalid"));
    } catch (Exception e) {
      return ResponseEntity.status(503).body(Map.of("status", "DOWN", "reason", e.getMessage()));
    }
  }
}
