plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "me.bombom"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:1.19.7")
    }
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.security:spring-security-test")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // spring security
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.nimbusds:nimbus-jose-jwt:10.4.2")

    // prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")

    // db
    runtimeOnly("com.mysql:mysql-connector-j")

    // spring session jdbc
    implementation ("org.springframework.session:spring-session-jdbc")

    // flyway
    implementation ("org.flywaydb:flyway-core")
    implementation ("org.flywaydb:flyway-mysql")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // logging
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.0"))
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    //ShedLock
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.9.2")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.9.2")

    //otel
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.7.0")

    // for : webhook
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // FCM
    implementation("com.google.firebase:firebase-admin:9.7.0")
}

// Querydsl 생성된 파일 정리
tasks.named<Delete>("clean") {
    delete("src/main/generated")
}

tasks.test {
    useJUnitPlatform()
}
