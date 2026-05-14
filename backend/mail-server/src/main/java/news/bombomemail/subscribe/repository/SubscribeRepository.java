package news.bombomemail.subscribe.repository;

import java.util.Optional;
import news.bombomemail.subscribe.domain.Subscribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    boolean existsByNewsletterIdAndMemberId(Long newsletterId, Long memberId);

    Optional<Subscribe> findByMemberIdAndNewsletterId(Long memberId, Long newsletterId);
}
