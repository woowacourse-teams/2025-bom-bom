package news.bombomemail.common.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.File;
import java.util.UUID;

/**
 * 이메일 처리 과정의 MDC(Mapped Diagnostic Context) 관리 유틸리티
 * 
 * traceId와 파일 정보를 MDC에 설정/해제하여 로깅에서 추적 가능하도록 지원
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailProcessingContext {
    
    // MDC 키 상수
    private static final String TRACE_ID_KEY = "traceId";
    private static final String FILE_NAME_KEY = "fileName";
    private static final String FILE_PATH_KEY = "filePath";
    
    /**
     * 이메일 파일 처리를 위한 MDC 컨텍스트 설정
     * 
     * @param emailFile 처리할 이메일 파일
     * @return 생성된 traceId (로깅이나 추가 처리에서 사용 가능)
     */
    public static String setupContext(File emailFile) {
        String traceId = generateTraceId();
        String fileName = emailFile.getName();
        String filePath = emailFile.getAbsolutePath();
        
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(FILE_NAME_KEY, fileName);
        MDC.put(FILE_PATH_KEY, filePath);
        
        log.debug("이메일 처리 컨텍스트 설정 완료 - traceId: {}, fileName: {}", traceId, fileName);
        
        return traceId;
    }
    
    /**
     * MDC 컨텍스트 정리
     * 메모리 누수 방지를 위해 처리 완료 후 반드시 호출해야 함
     */
    public static void clearContext() {
        String traceId = getCurrentTraceId();
        String fileName = getCurrentFileName();
        
        MDC.clear();
        
        log.debug("이메일 처리 컨텍스트 정리 완료 - traceId: {}, fileName: {}", traceId, fileName);
    }
    
    /**
     * 현재 설정된 traceId 조회
     * 
     * @return 현재 traceId, 설정되지 않았으면 null
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * 현재 설정된 파일명 조회
     * 
     * @return 현재 파일명, 설정되지 않았으면 null
     */
    public static String getCurrentFileName() {
        return MDC.get(FILE_NAME_KEY);
    }
    
    /**
     * 현재 설정된 파일 경로 조회
     * 
     * @return 현재 파일 경로, 설정되지 않았으면 null
     */
    public static String getCurrentFilePath() {
        return MDC.get(FILE_PATH_KEY);
    }
    
    /**
     * 8자리 고유 traceId 생성
     * 
     * @return 8자리 알파벳-숫자 조합의 traceId
     */
    private static String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * 람다 스타일의 컨텍스트 관리를 위한 헬퍼
     * 
     * @param emailFile 처리할 이메일 파일
     * @param processor 이메일 처리 로직
     */
    public static void executeWithContext(File emailFile, Runnable processor) {
        setupContext(emailFile);
        try {
            processor.run();
        } finally {
            clearContext();
        }
    }
    
    /**
     * 예외를 던질 수 있는 처리 로직을 위한 헬퍼
     * 
     * @param emailFile 처리할 이메일 파일
     * @param processor 이메일 처리 로직 (예외 던질 수 있음)
     * @param <E> 예외 타입
     * @throws E 처리 로직에서 발생한 예외
     */
    public static <E extends Exception> void executeWithContextThrows(File emailFile, ThrowingRunnable<E> processor) throws E {
        setupContext(emailFile);
        try {
            processor.run();
        } finally {
            clearContext();
        }
    }
    
    /**
     * 예외를 던질 수 있는 Runnable 인터페이스
     * 
     * @param <E> 예외 타입
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception> {
        void run() throws E;
    }
}
