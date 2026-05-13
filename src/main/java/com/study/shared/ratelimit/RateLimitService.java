// com.study.shared.ratelimit.RateLimitService
package com.study.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 유저별 API 요청 빈도 제한 서비스.
 *
 * <p>현재 구현: ConcurrentHashMap + Bucket4j (인메모리). 서버 재시작 시 버킷이 초기화된다.
 *
 * <p>운영 전환 시 Redis 백엔드로 교체:
 * <pre>
 * // 1. build.gradle: implementation 'com.bucket4j:bucket4j-redis:8.x.x'
 * // 2. RedisTemplate<String, byte[]> 빈 주입
 * // 3. ProxyManager<String> proxyManager = Bucket4jRedis.casBasedBuilder(redisTemplate).build();
 * // 4. Bucket bucket = proxyManager.builder().addLimit(...).build(userId.toString());
 * </pre>
 */
@Service
public class RateLimitService {

  private static final int CAPACITY = 5;
  private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

  private final ConcurrentHashMap<Long, Bucket> buckets = new ConcurrentHashMap<>();

  /**
   * 주어진 사용자 ID에 대해 토큰 1개 소비 시도.
   *
   * @return 소비 성공(요청 허용)이면 true, 버킷 소진(Rate Limit 초과)이면 false
   */
  public boolean tryConsume(Long userId) {
    Bucket bucket = buckets.computeIfAbsent(userId, this::newBucket);
    return bucket.tryConsume(1);
  }

  private Bucket newBucket(Long ignored) {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(CAPACITY)
                .refillGreedy(CAPACITY, REFILL_PERIOD)
                .build())
        .build();
  }
}