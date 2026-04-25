package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface MaeilMailSentContentRepository
        extends JpaRepository<MaeilMailSentContent, Long>, CustomMaeilMailSentContentRepository {

    @Modifying
    void deleteByMemberIdAndTopicId(Long memberId, Long topicId);
}
