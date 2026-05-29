# 빌드
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

# 의존성 캐싱 최적화를 위해 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

# 실행
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
