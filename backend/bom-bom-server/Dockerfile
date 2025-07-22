# 1. Amazon Corretto 21 기반
FROM amazoncorretto:21

# 2. 타임존 설정 (OS + JVM)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 빌드 시 JAR 경로를 외부에서 주입 가능하게 설정
ARG JAR_FILE=build/libs/*.jar

# 4. 작업 디렉토리 설정
WORKDIR /app

# 5. JAR 파일 복사 (app.jar로 고정)
COPY ${JAR_FILE} app.jar

# 6. 외부 포트 노출 (문서 목적 + docker run -P 대비)
EXPOSE 8080

# 7. Spring Boot 실행 (타임존 JVM 옵션 포함)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
