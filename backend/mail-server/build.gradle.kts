plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "news.bombomemail"
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

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // spring integration
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.integration:spring-integration-http")
    implementation("org.springframework.integration:spring-integration-jpa")
    implementation("org.springframework.integration:spring-integration-file")

    // spring mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // html parser
    implementation("org.jsoup:jsoup:1.21.1")

    // test
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.security:spring-security-test")

    // db
    runtimeOnly("com.mysql:mysql-connector-j")

    // webclient
    implementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
