package news.bombomemail.nativenewsletter.maeilmail.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;

public interface CustomMaeilMailIssueHistoryRepository {

    Set<MemberTopicKey> findIssuedMemberTopicKeys(
            LocalDate issueDate,
            Collection<MemberTopicKey> keys
    );
}
