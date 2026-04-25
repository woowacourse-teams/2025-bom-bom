package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;

public interface CustomMaeilMailSentContentRepository {

    List<MaeilMailSentContent> findAllByMemberTopicKeys(Collection<MemberTopicKey> keys);
}
