package me.bombom.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainerConfig {

    private static final String MYSQL_IMAGE = "mysql:8.4.5";
    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE));
        MYSQL_CONTAINER.start();

        System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl());
        System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
        System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
    }
}
