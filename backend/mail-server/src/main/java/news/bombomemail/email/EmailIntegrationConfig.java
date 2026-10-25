package news.bombomemail.email;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.email.service.EmailService.DiscardedEmailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice.MessageHandlingExpressionEvaluatingAdviceException;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.CannotCreateTransactionException;

@Slf4j
@Profile("!test")
@Configuration
@EnableIntegration
public class EmailIntegrationConfig {

    private static final String DIR_NEW = "new";
    private static final String DIR_IN_PROGRESS = "in-progress";
    private static final String DIR_PROCESSED = "processed";
    private static final String DIR_FAILED = "parsing-failed";
    private static final String DIR_DISCARDED = "discarded";

    private static final long POLL_DELAY_MILLIS = 1000;
    // 한 스레드에서 순서대로 처리하므로 한 번의 폴링에서 여러 통을 연달아 처리해도 같은 파일을 두 번 읽지 않는다.
    // 저장 1통 평균 약 80ms(2026-10 운영 로그 실측)라 한 번의 폴링은 약 1.6초만 스케줄러 스레드를 점유한다.
    // 적게 자주 읽어 다른 @Scheduled 작업이 기다리는 시간을 줄이고, 처리량은 초당 약 7통을 유지한다.
    private static final int MAX_MESSAGES_PER_POLL = 20;

    // DB 연결 실패·타임아웃·락 대기 실패처럼 메일 내용과 상관없이 다시 시도하면 성공할 수 있는 예외.
    // 연결을 얻지 못하면 DataAccessException 이 아닌 CannotCreateTransactionException 이 나서 따로 둔다.
    private static final List<Class<? extends Throwable>> RETRYABLE_EXCEPTIONS = List.of(
            CannotCreateTransactionException.class,
            TransientDataAccessException.class,
            RecoverableDataAccessException.class,
            DataAccessResourceFailureException.class
    );

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

        // 파일은 읽자마자 in-progress 로 이동하므로 AcceptOnce 필터를 두지 않는다.
        // 필터가 있으면 InProgressRequeueScheduler 가 new 로 되돌린 파일을 재시작 전까지 다시 읽지 않는다.
        source.setFilter(new SimplePatternFileListFilter("*"));

        return source;
    }

    @Bean
    public ExpressionEvaluatingRequestHandlerAdvice emailProcessingAdvice() {
        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();

        // 성공: 원본 payload(File)를 그대로 processedChannel 로 전달
        advice.setOnSuccessExpressionString("payload");
        advice.setSuccessChannelName("processedChannel");

        // 실패: 원본 메시지 기준으로 평가된 payload(File)가 ErrorMessage 의 evaluationResult 로 failedChannel 에 전달
        advice.setOnFailureExpressionString("payload");
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
                        c -> c.poller(Pollers.fixedDelay(POLL_DELAY_MILLIS)
                                .maxMessagesPerPoll(MAX_MESSAGES_PER_POLL)
                                .getObject()))
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

    /**
     * 실패 채널에는 원본 File 이 아니라 MessageHandlingExpressionEvaluatingAdviceException 을 담은 ErrorMessage 가 온다.
     * 실패 표현식의 결과(원본 File)는 evaluationResult 에 들어 있다.
     */
    @Bean
    public IntegrationFlow failedMoveFlow() {
        return IntegrationFlow
                .from("failedChannel")
                .route(MessageHandlingExpressionEvaluatingAdviceException.class,
                        this::classifyFailure,
                        router -> router
                                .subFlowMapping(FailureType.DISCARDED, flow -> moveEvaluatedFile(flow, DIR_DISCARDED))
                                .subFlowMapping(FailureType.RETRYABLE, this::keepInProgress)
                                .subFlowMapping(FailureType.FAILED, flow -> moveEvaluatedFile(flow, DIR_FAILED)))
                .get();
    }

    private FailureType classifyFailure(MessageHandlingExpressionEvaluatingAdviceException exception) {
        if (exception.contains(DiscardedEmailException.class)) {
            return FailureType.DISCARDED;
        }
        if (RETRYABLE_EXCEPTIONS.stream().anyMatch(exception::contains)) {
            return FailureType.RETRYABLE;
        }
        return FailureType.FAILED;
    }

    /**
     * DB 연결 끊김처럼 다시 시도하면 성공할 수 있는 실패는 파일을 in-progress 에 그대로 둔다.
     * InProgressRequeueScheduler 가 기준 시간이 지난 파일을 new 로 되돌려 다시 처리한다.
     */
    private void keepInProgress(IntegrationFlowDefinition<?> flow) {
        flow.handle(message -> {
            MessageHandlingExpressionEvaluatingAdviceException exception =
                    (MessageHandlingExpressionEvaluatingAdviceException) message.getPayload();
            log.warn("일시적 오류로 in-progress 에 남김, requeue 후 재처리: {}",
                    exception.getEvaluationResult(), exception.getMostSpecificCause());
        });
    }

    private void moveEvaluatedFile(IntegrationFlowDefinition<?> flow, String dirName) {
        flow.transform(MessageHandlingExpressionEvaluatingAdviceException.class,
                        MessageHandlingExpressionEvaluatingAdviceException::getEvaluationResult)
                .handle(Files.outboundAdapter(new File(mailBaseDir, dirName))
                        .autoCreateDirectory(true)
                        .deleteSourceFiles(true));
    }

    private enum FailureType {
        DISCARDED,
        RETRYABLE,
        FAILED
    }
}
