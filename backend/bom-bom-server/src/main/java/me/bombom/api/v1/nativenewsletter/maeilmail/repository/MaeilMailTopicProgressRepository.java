package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.Optional;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaeilMailTopicProgressRepository extends JpaRepository<MaeilMailTopicProgress, Long> {

    Optional<MaeilMailTopicProgress> findByMemberIdAndTopicId(Long memberId, Long topicId);
}
