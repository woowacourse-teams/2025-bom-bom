FROM eclipse-temurin:21-jdk-alpine

ARG APP_USER=app
ARG APP_UID=1001
ARG APP_GID=1001

RUN addgroup -g ${APP_GID} ${APP_USER} \
 && adduser -u ${APP_UID} -G ${APP_USER} -s /bin/sh -D ${APP_USER}

WORKDIR /app
COPY build/libs/*.jar app.jar

USER ${APP_USER}

ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=docker"]
