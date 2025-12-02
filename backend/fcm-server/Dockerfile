# 1. 경량 Java 21 런타임 (멀티 아키텍처 지원)
FROM amazoncorretto:21-alpine-jdk

# 2. 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

# 3. 작업 디렉토리
WORKDIR /app

# 4. 빌드된 JAR 복사 (CI에서 빌드 완료된 상태라고 가정)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 5. 로그/키/설정 디렉토리 (볼륨 마운트 시에도 디렉토리 미리 만들어 두면 깔끔)
RUN mkdir -p /logs /config /app/key \
    && addgroup -S app && adduser -S app -G app \
    && chown -R app:app /app /logs /config

# 6. non-root 유저로 실행
USER app

# 7. 포트 노출 (문서용)
EXPOSE 8080

# 8. 애플리케이션 실행
#   - env로 JAVA_OPTS, SPRING_CONFIG_ADDITIONAL_LOCATION, GOOGLE_APPLICATION_CREDENTIALS 넘길 수 있음
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=Asia/Seoul -jar app.jar"]
