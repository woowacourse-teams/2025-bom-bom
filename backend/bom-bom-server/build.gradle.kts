plugins {
    java
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.10.0"
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
        mavenBom("org.testcontainers:testcontainers-bom:2.0.3")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:2.26.1-alpha")
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
    testImplementation("org.testcontainers:testcontainers-mysql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // db
    runtimeOnly("com.mysql:mysql-connector-j")

    // spring session jdbc
    implementation("org.springframework.session:spring-session-jdbc")

    // flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // openapi generator 런타임 의존성 (generated 코드가 import)
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    // logging
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.0"))
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    //ShedLock
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.9.2")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.9.2")

    //otel
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0")

    // AWS SDK
    implementation(platform("software.amazon.awssdk:bom:2.41.21"))
    implementation("software.amazon.awssdk:lambda")

    // Annotations
    implementation("jakarta.annotation:jakarta.annotation-api")
}

// Querydsl + OpenAPI 생성 파일 정리
tasks.named<Delete>("clean") {
    delete("src/main/generated")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/openapi-spec/openapi.yaml")
    outputDir.set("$projectDir/src/main/generated/openapi")
    apiPackage.set("me.bombom.openapi.api")
    modelPackage.set("me.bombom.openapi.model")
    templateDir.set("$projectDir/src/main/resources/openapi-templates")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useTags" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "useResponseEntity" to "false",
            "useSpringController" to "false",
            "openApiNullable" to "false",
            "skipDefaultInterface" to "true",
            "dateLibrary" to "java8",
            "documentationProvider" to "springdoc",
            "annotationLibrary" to "swagger2",
        )
    )
    globalProperties.set(
        mapOf(
            "apiDocs" to "false",
            "modelDocs" to "false",
            "apiTests" to "false",
            "modelTests" to "false",
        )
    )
}

sourceSets {
    main {
        java {
            srcDirs("src/main/generated/openapi/src/main/java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

tasks.test {
    useJUnitPlatform()
}
