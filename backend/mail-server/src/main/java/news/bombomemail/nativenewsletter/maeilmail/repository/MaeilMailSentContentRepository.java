package news.bombomemail.nativenewsletter.maeilmail.repository;

import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface MaeilMailSentContentRepository
        extends JpaRepository<MaeilMailSentContent, Long>, CustomMaeilMailSentContentRepository {

    @Modifying
    void deleteByMemberIdAndTopicId(Long memberId, Long topicId);
}
