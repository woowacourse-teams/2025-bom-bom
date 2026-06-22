package me.bombom.support.integration;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트용 MySQL Testcontainers를 시작하고 Gradle worker별 데이터베이스를 설정한다.
 */
@TestConfiguration
public class TestcontainerConfig {

    private static final String MYSQL_IMAGE = "mysql:8.4.5";
    private static final int TEST_WORKER_SLOTS = 2;
    private static final boolean REUSE_ENABLED = Boolean.getBoolean("bombom.testcontainers.reuse");
    private static final String TEST_WORKER_SLOT = testWorkerSlot();
    private static final MySQLContainer MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName("test")
                .withReuse(REUSE_ENABLED);
        startContainer();
        createWorkerDatabase();

        System.setProperty("spring.datasource.url", workerJdbcUrl());
        System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
        System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
    }

    private static void startContainer() {
        if (!REUSE_ENABLED) {
            MYSQL_CONTAINER.start();
            return;
        }

        Path lockFile = Path.of(System.getProperty("java.io.tmpdir"), "bombom-mysql-testcontainer.lock");
        try (FileChannel channel = FileChannel.open(
                        lockFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE
                );
                FileLock ignored = channel.lock()) {
            MYSQL_CONTAINER.start();
        } catch (IOException exception) {
            throw new IllegalStateException("재사용 MySQL 테스트 컨테이너를 시작하지 못했습니다.", exception);
        }
    }

    private static void createWorkerDatabase() {
        String database = workerDatabase();
        try {
            MYSQL_CONTAINER.execInContainer(
                    "mysql",
                    "-uroot",
                    "-p" + MYSQL_CONTAINER.getPassword(),
                    "-e",
                    "CREATE DATABASE IF NOT EXISTS `" + database + "`; "
                            + "GRANT ALL PRIVILEGES ON `" + database + "`.* TO '"
                            + MYSQL_CONTAINER.getUsername() + "'@'%';"
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테스트 worker 전용 데이터베이스를 만들지 못했습니다.", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("테스트 worker 전용 데이터베이스를 만들지 못했습니다.", exception);
        }
    }

    private static String workerJdbcUrl() {
        return MYSQL_CONTAINER.getJdbcUrl().replaceFirst("/test(?=\\?|$)", "/" + workerDatabase());
    }

    private static String workerDatabase() {
        return "test_worker_" + TEST_WORKER_SLOT;
    }

    private static String testWorkerSlot() {
        String workerId = System.getProperty("org.gradle.test.worker");
        if (workerId == null) {
            return "standalone";
        }

        try {
            return String.valueOf(Math.floorMod(Integer.parseInt(workerId) - 1, TEST_WORKER_SLOTS) + 1);
        } catch (NumberFormatException exception) {
            return workerId.replaceAll("[^A-Za-z0-9_]", "_");
        }
    }
}
