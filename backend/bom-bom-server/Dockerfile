# ✅ 1. Amazon Corretto 21 기반 (검토 필요)
FROM amazoncorretto:21

# ✅ 2. 빌드 시 JAR 경로를 외부에서 주입 가능하게 설정
ARG JAR_FILE=build/libs/*.jar

# ✅ 3. 작업 디렉토리 설정
WORKDIR /app

# ✅ 4. JAR 파일 복사 (app.jar로 고정)
COPY ${JAR_FILE} app.jar

# ✅ 5. 외부 포트 노출 (문서 목적 + docker run -P 대비)
EXPOSE 8080

# ✅ 6. Spring Boot 실행 최적화 옵션 포함
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]


