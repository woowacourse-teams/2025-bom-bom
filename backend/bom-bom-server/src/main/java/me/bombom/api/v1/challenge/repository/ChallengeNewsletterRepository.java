package me.bombom.api.v1.challenge.repository;

import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeNewsletter;
import me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeNewsletterRepository extends JpaRepository<ChallengeNewsletter, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.challenge.dto.ChallengeNewsletterRow(
            cn.challengeId,
            n.id,
            n.name,
            n.imageUrl
        )
        FROM ChallengeNewsletter cn
        JOIN Newsletter n ON n.id = cn.newsletterId
        WHERE cn.challengeId IN :challengeIds
    """)
    List<ChallengeNewsletterRow> findNewsletterResponsesByChallengeIds(
            @Param("challengeIds") List<Long> challengeIds
    );
}
