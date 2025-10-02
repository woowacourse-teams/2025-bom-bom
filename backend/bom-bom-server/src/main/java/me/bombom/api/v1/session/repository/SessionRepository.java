package me.bombom.api.v1.session.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 전체 세션 수 조회
     * 
     * @return 전체 세션 수
     */
    public int countTotalSessions() {
        String sql = "SELECT COUNT(*) FROM SPRING_SESSION";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * 만료된 세션 수 조회
     * 
     * @param currentTime 현재 시간 (밀리초)
     * @return 만료된 세션 수
     */
    public int countExpiredSessions(long currentTime) {
        String sql = "SELECT COUNT(*) FROM SPRING_SESSION WHERE EXPIRY_TIME < ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, currentTime);
        return count != null ? count : 0;
    }

    /**
     * 만료된 세션 삭제
     * 
     * @param currentTime 현재 시간 (밀리초)
     * @return 삭제된 세션 수
     */
    public int deleteExpiredSessions(long currentTime) {
        String sql = "DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME < ?";
        return jdbcTemplate.update(sql, currentTime);
    }

    /**
     * 만료된 세션과 오래된 세션 모두 삭제 (철저한 정리)
     * 
     * @param currentTime 현재 시간 (밀리초)
     * @param oldTime 오래된 세션 기준 시간 (밀리초)
     * @return 삭제된 세션 수
     */
    public int deleteExpiredAndOldSessions(long currentTime, long oldTime) {
        String sql = """
            DELETE FROM SPRING_SESSION 
            WHERE EXPIRY_TIME < ? 
               OR CREATION_TIME < ?
            """;
        return jdbcTemplate.update(sql, currentTime, oldTime);
    }
}
