# 1. Amazon Corretto 21 기반
FROM amazoncorretto:21

# 2. 타임존 설정 (OS + JVM)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 빌드 시 JAR 경로를 외부에서 주입 가능하게 설정
ARG JAR_FILE=build/libs/*.jar

# 4. 작업 디렉토리 설정
WORKDIR /app

# 5. JAR를 /app/app.jar + /app/lib/로 풀어 둔다 (CDS는 풀어 둔 구조에서 동작)
COPY ${JAR_FILE} /tmp/app.jar
RUN java -Djarmode=tools -jar /tmp/app.jar extract --destination /app && rm /tmp/app.jar

# 6. CDS 학습 실행: 컨텍스트만 띄웠다 끄면서 로드한 클래스를 app.jsa로 저장한다
#    DB에 접속하지 않도록 가짜 값으로 된 학습 전용 설정을 쓰고, Apple 키는 빌드마다 일회용으로 만든다
COPY cds/ /tmp/cds/
RUN CDS_TRAINING_APPLE_PRIVATE_KEY="$(java /tmp/cds/TrainingAppleKey.java)" \
    java -XX:ArchiveClassesAtExit=app.jsa -XX:+UseG1GC -Xmx512m -XX:MaxMetaspaceSize=256m \
      -Dspring.context.exit=onRefresh -Dspring.config.location=file:/tmp/cds/application-cds-training.yml \
      -jar app.jar \
    && rm -rf /tmp/cds

# 7. 외부 포트 노출 (문서 목적 + docker run -P 대비)
EXPOSE 8080

# 8. Spring Boot 실행 (타임존 JVM 옵션 + CDS 아카이브)
ENTRYPOINT ["java", "-XX:SharedArchiveFile=app.jsa", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
