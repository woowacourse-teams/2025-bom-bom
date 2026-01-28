# 1. Amazon Corretto 21 (Amazon Linux 2023 기반)
FROM amazoncorretto:21-al2023

# 2. 타임존 설정 (OS + JVM)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 2-1. Playwright 실행을 위한 OS 의존성 설치 (Amazon Linux 2023 호환)
# Chromium 구동에 필요한 최소 런타임 라이브러리 (폰트 및 GUI 라이브러리 포함)
RUN dnf install -y \
    alsa-lib atk at-spi2-atk at-spi2-core cups-libs \
    gtk3 pango nss libXcomposite libXcursor libXdamage libXext libXi libXrandr libXScrnSaver libXtst \
    libdrm libgbm libxkbcommon mesa-libgbm \
    xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-fonts-cyrillic xorg-x11-fonts-misc xorg-x11-fonts-Type1 xorg-x11-utils \
    unzip \
    && dnf clean all

# 3. 빌드 시 JAR 경로를 외부에서 주입 가능하게 설정
ARG JAR_FILE=build/libs/*.jar

# 4. 작업 디렉토리 설정
WORKDIR /app

# 5. JAR 파일 복사 (app.jar로 고정)
COPY ${JAR_FILE} app.jar

# 5-1. Playwright 브라우저 미리 설치 (빌드 시점)
# Spring Boot Fat JAR 구조에서는 직접 클래스 실행이 어려우므로 압축 해제 후 실행
RUN unzip -q app.jar -d temp_lib && \
    java -cp "temp_lib/BOOT-INF/lib/*:temp_lib/BOOT-INF/classes" com.microsoft.playwright.CLI install chromium && \
    rm -rf temp_lib

# 5-2. 런타임 시 브라우저 다운로드 스킵 설정
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true

# 6. 외부 포트 노출 (문서 목적 + docker run -P 대비)
EXPOSE 8080

# 7. Spring Boot 실행 (타임존 JVM 옵션 포함)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
