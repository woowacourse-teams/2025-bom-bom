package news.bombomemail.email;

import jakarta.annotation.PostConstruct;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.messaging.MessageChannel;

@Slf4j
@Profile("!test")
@Configuration
@EnableIntegration
public class EmailIntegrationConfig {

    private static final String DIR_NEW = "new";
    private static final String DIR_IN_PROGRESS = "in-progress";
    private static final String DIR_PROCESSED = "processed";
    private static final String DIR_FAILED = "parsing-failed";

    /**
     * maildir의 base 디렉터리. 예) /maildir
     */
    @Value("${maildir.base-dir}")
    private String mailBaseDir;

    @PostConstruct
    public void logMailDir() {
        log.info("▶▶▶ maildir.base-dir = {}", mailBaseDir);
        File dir = new File(mailBaseDir);
        log.info("▶▶▶ 실제 디렉토리: {} (exists: {}, writable: {})",
                dir.getAbsolutePath(), dir.exists(), dir.canWrite());
    }

    @Bean
    public MessageChannel processedChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel failedChannel() {
        return new DirectChannel();
    }

    /**
     * maildir/new 를 읽어오는 소스.
     */
    @Bean
    public MessageSource<File> newMailFileSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(mailBaseDir, DIR_NEW));
        source.setAutoCreateDirectory(true);

        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>();
        filter.addFilter(new SimplePatternFileListFilter("*"));
        filter.addFilter(new AcceptOnceFileListFilter<>());
        source.setFilter(filter);

        return source;
    }

    @Bean
    public ExpressionEvaluatingRequestHandlerAdvice emailProcessingAdvice() {
        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();

        // 성공: 원본 payload(File)를 그대로 processedChannel 로 전달
        advice.setOnSuccessExpressionString("payload");
        advice.setSuccessChannelName("processedChannel");

        // 실패: ErrorMessage.payload.failedMessage.payload (원본 File)을 failedChannel 로 전달
        advice.setOnFailureExpressionString("payload.failedMessage.payload");
        advice.setFailureChannelName("failedChannel");

        advice.setTrapException(true);

        return advice;
    }

    /**
     * maildir/new → maildir/in-progress 로 이동시키고, 이어서 처리까지 수행하는 플로우
     */
    @Bean
    public IntegrationFlow mailFileFlow() {
        File inProgressDir = new File(mailBaseDir, DIR_IN_PROGRESS);

        return IntegrationFlow
                .from(newMailFileSource(),
                        c -> c.poller(Pollers.fixedDelay(5000).getObject()))
                .handle(Files.outboundGateway(inProgressDir)
                        .autoCreateDirectory(true)
                        .deleteSourceFiles(true))
                .handle("emailService", "processEmailFile",
                        e -> e.advice(emailProcessingAdvice()))
                .get();
    }

    @Bean
    public IntegrationFlow processedMoveFlow() {
        File processedDir = new File(mailBaseDir, DIR_PROCESSED);

        return IntegrationFlow
                .from("processedChannel")
                .handle(Files.outboundAdapter(processedDir)
                        .autoCreateDirectory(true)
                        .deleteSourceFiles(true))
                .get();
    }

    @Bean
    public IntegrationFlow failedMoveFlow() {
        File failedDir = new File(mailBaseDir, DIR_FAILED);

        return IntegrationFlow
                .from("failedChannel")
                .handle(Files.outboundAdapter(failedDir)
                        .autoCreateDirectory(true)
                        .deleteSourceFiles(true))
                .get();
    }
}
