# ── Stage 1: Build ──────────────────────────────────────────────────────────
# JDK + Gradle로 소스를 컴파일해 실행 가능한 JAR를 만드는 단계
# 이 이미지 자체는 최종 결과물에 포함되지 않음
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

COPY src src

RUN ./gradlew bootJar --no-daemon -x test -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Stage 1에서 만든 JAR만 꺼내 JRE 위에서 실행하는 단계
# JDK·Gradle·소스코드 없이 JAR + JRE만 남기므로 이미지 크기가 절반 이하로 줄어듦
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
