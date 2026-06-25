package news.bombomemail.nativenewsletter.maeilmail.repository;

import java.util.Collection;
import java.util.List;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;

public interface CustomMaeilMailSentContentRepository {

    List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys);
}
