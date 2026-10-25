package news.bombomemail.email;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import news.bombomemail.email.service.EmailService;
import news.bombomemail.email.service.EmailService.DiscardedEmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.CannotCreateTransactionException;

@SpringJUnitConfig(EmailIntegrationConfigTest.TestConfig.class)
class EmailIntegrationConfigTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static final Path BASE_DIR = createBaseDir();

    @Autowired
    EmailService emailService;

    @DynamicPropertySource
    static void maildirProperties(DynamicPropertyRegistry registry) {
        registry.add("maildir.base-dir", BASE_DIR::toString);
    }

    @BeforeEach
    void setUp() {
        reset(emailService);
    }

    @Test
    void 처리에_성공한_메일은_processed로_이동한다() throws Exception {
        // given
        doNothing().when(emailService).processEmailFile(fileNamed("success.eml"));

        // when
        deliver("success.eml");

        // then
        awaitMovedTo("processed", "success.eml");
    }

    @Test
    void 폐기_대상_메일은_discarded로_이동한다() throws Exception {
        // given
        doThrow(new DiscardedEmailException("폐기"))
                .when(emailService).processEmailFile(fileNamed("discarded.eml"));

        // when
        deliver("discarded.eml");

        // then
        awaitMovedTo("discarded", "discarded.eml");
    }

    @Test
    void 처리에_실패한_메일은_parsing_failed로_이동한다() throws Exception {
        // given
        doThrow(new IllegalStateException("파싱 실패"))
                .when(emailService).processEmailFile(fileNamed("failed.eml"));

        // when
        deliver("failed.eml");

        // then
        awaitMovedTo("parsing-failed", "failed.eml");
    }

    @Test
    void DB_연결_실패로_처리하지_못한_메일은_in_progress에_남는다() throws Exception {
        // given
        doThrow(new CannotCreateTransactionException("DB 연결 실패"))
                .when(emailService).processEmailFile(fileNamed("db-down.eml"));

        // when
        deliver("db-down.eml");

        // then
        verify(emailService, timeout(TIMEOUT.toMillis())).processEmailFile(fileNamed("db-down.eml"));
        await().during(Duration.ofSeconds(2))
                .atMost(TIMEOUT)
                .until(() -> Files.exists(BASE_DIR.resolve("in-progress").resolve("db-down.eml"))
                        && Files.notExists(BASE_DIR.resolve("parsing-failed").resolve("db-down.eml")));
    }

    @Test
    void new로_되돌아온_같은_이름의_메일도_다시_처리한다() throws Exception {
        // given
        doNothing().when(emailService).processEmailFile(fileNamed("requeued.eml"));
        deliver("requeued.eml");
        awaitMovedTo("processed", "requeued.eml");
        Files.delete(BASE_DIR.resolve("processed").resolve("requeued.eml"));

        // when
        deliver("requeued.eml");

        // then
        awaitMovedTo("processed", "requeued.eml");
    }

    private static void deliver(String fileName) throws IOException {
        Path newDir = Files.createDirectories(BASE_DIR.resolve("new"));
        Files.writeString(newDir.resolve(fileName), "Subject: test\r\n\r\nbody");
    }

    private static void awaitMovedTo(String dirName, String fileName) {
        await().atMost(TIMEOUT)
                .until(() -> Files.exists(BASE_DIR.resolve(dirName).resolve(fileName))
                        && Files.notExists(BASE_DIR.resolve("new").resolve(fileName))
                        && Files.notExists(BASE_DIR.resolve("in-progress").resolve(fileName)));
    }

    private static File fileNamed(String fileName) {
        return argThat(file -> file != null && file.getName().equals(fileName));
    }

    private static Path createBaseDir() {
        try {
            return Files.createTempDirectory("maildir-test");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Configuration
    @Import(EmailIntegrationConfig.class)
    static class TestConfig {

        @Bean
        EmailService emailService() {
            return mock(EmailService.class);
        }
    }
}
