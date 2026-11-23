package me.bombom.api.v1.signupsource.repository;

import me.bombom.api.v1.signupsource.domain.MemberSignupSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberSignupSourceRepository extends JpaRepository<MemberSignupSource, Long> {

    boolean existsByMemberId(Long memberId);

    /**
     * 회원당 한 행만 남긴다. 같은 회원의 요청이 동시에 들어와도 유니크 키 충돌 예외 없이 먼저 저장된 값을 유지한다.
     */
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO member_signup_source (member_id, source, detail)
            VALUES (:memberId, :source, :detail)
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("memberId") Long memberId,
            @Param("source") String source,
            @Param("detail") String detail
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE MemberSignupSource s
            SET s.detail = NULL
            WHERE s.memberId = :memberId
            """)
    void clearDetailByMemberId(@Param("memberId") Long memberId);
}
