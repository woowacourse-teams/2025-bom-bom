package news.bombomemail.common.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ActiveProfiles("test")
class EmailProcessingContextTest {

    @Test
    void MDC_컨텍스트_설정_및_정리() throws Exception {
        // given
        Path tempFile = Files.createTempFile("test-email-", ".eml");
        File emailFile = tempFile.toFile();
        
        try {
            // when
            String traceId = EmailProcessingContext.setupContext(emailFile);
            
            // then
            assertSoftly(soft -> {
                soft.assertThat(traceId).isNotNull().hasSize(8);
                soft.assertThat(EmailProcessingContext.getCurrentTraceId()).isEqualTo(traceId);
                soft.assertThat(EmailProcessingContext.getCurrentFileName()).isEqualTo(emailFile.getName());
                soft.assertThat(EmailProcessingContext.getCurrentFilePath()).isEqualTo(emailFile.getAbsolutePath());
                soft.assertThat(MDC.get("traceId")).isEqualTo(traceId);
                soft.assertThat(MDC.get("fileName")).isEqualTo(emailFile.getName());
                soft.assertThat(MDC.get("filePath")).isEqualTo(emailFile.getAbsolutePath());
            });
            
            // when
            EmailProcessingContext.clearContext();
            
            // then
            assertSoftly(soft -> {
                soft.assertThat(EmailProcessingContext.getCurrentTraceId()).isNull();
                soft.assertThat(EmailProcessingContext.getCurrentFileName()).isNull();
                soft.assertThat(EmailProcessingContext.getCurrentFilePath()).isNull();
                soft.assertThat(MDC.get("traceId")).isNull();
                soft.assertThat(MDC.get("fileName")).isNull();
                soft.assertThat(MDC.get("filePath")).isNull();
            });
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void executeWithContext_정상_실행() throws Exception {
        // given
        Path tempFile = Files.createTempFile("test-email-", ".eml");
        File emailFile = tempFile.toFile();
        boolean[] executed = {false};
        
        try {
            // when
            EmailProcessingContext.executeWithContext(emailFile, () -> {
                // then - 실행 중에는 컨텍스트가 설정되어 있어야 함
                assertThat(EmailProcessingContext.getCurrentTraceId()).isNotNull();
                assertThat(EmailProcessingContext.getCurrentFileName()).isEqualTo(emailFile.getName());
                executed[0] = true;
            });
            
            // then - 실행 후에는 컨텍스트가 정리되어 있어야 함
            assertThat(executed[0]).isTrue();
            assertThat(EmailProcessingContext.getCurrentTraceId()).isNull();
            assertThat(EmailProcessingContext.getCurrentFileName()).isNull();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void executeWithContext_예외_발생시에도_정리됨() throws Exception {
        // given
        Path tempFile = Files.createTempFile("test-email-", ".eml");
        File emailFile = tempFile.toFile();
        
        try {
            // when & then
            try {
                EmailProcessingContext.executeWithContextThrows(emailFile, () -> {
                    // 컨텍스트가 설정되어 있는지 확인
                    assertThat(EmailProcessingContext.getCurrentTraceId()).isNotNull();
                    // 의도적으로 예외 발생
                    throw new RuntimeException("테스트 예외");
                });
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("테스트 예외");
            }
            
            // then - 예외 발생 후에도 컨텍스트가 정리되어 있어야 함
            assertThat(EmailProcessingContext.getCurrentTraceId()).isNull();
            assertThat(EmailProcessingContext.getCurrentFileName()).isNull();
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
