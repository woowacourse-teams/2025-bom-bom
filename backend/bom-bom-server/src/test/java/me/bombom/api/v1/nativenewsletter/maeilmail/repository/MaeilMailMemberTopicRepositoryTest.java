package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class MaeilMailMemberTopicRepositoryTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 4, 24);

    @Autowired
    private MaeilMailSentContentRepository sentContentRepository;

    @Autowired
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @BeforeEach
    void setup() {
        issueHistoryRepository.deleteAllInBatch();
        sentContentRepository.deleteAllInBatch();
    }

    @Test
    void sent_content는_member_topic_pair를_정확히_조회한다() {
        sentContentRepository.saveAll(List.of(
                createSentContent(1L, 10L, 100L),
                createSentContent(1L, 20L, 101L),
                createSentContent(2L, 20L, 200L)
        ));

        List<MaeilMailSentContent> sentContents = sentContentRepository.findAllByMemberTopicKeys(List.of(
                new MemberTopicKey(1L, 10L),
                new MemberTopicKey(2L, 20L)
        ));

        assertThat(sentContents)
                .extracting(
                        MaeilMailSentContent::getMemberId,
                        MaeilMailSentContent::getTopicId,
                        MaeilMailSentContent::getContentId
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, 10L, 100L),
                        tuple(2L, 20L, 200L)
                );
    }

    @Test
    void issue_history는_issue_date와_member_topic_pair를_정확히_조회한다() {
        issueHistoryRepository.saveAll(List.of(
                createIssueHistory(ISSUE_DATE, 1L, 10L),
                createIssueHistory(ISSUE_DATE, 1L, 20L),
                createIssueHistory(ISSUE_DATE, 2L, 20L),
                createIssueHistory(ISSUE_DATE.minusDays(1), 1L, 10L)
        ));

        Set<MemberTopicKey> issuedKeys = issueHistoryRepository.findIssuedMemberTopicKeys(
                ISSUE_DATE,
                List.of(new MemberTopicKey(1L, 10L), new MemberTopicKey(2L, 20L))
        );

        assertThat(issuedKeys).containsExactlyInAnyOrder(
                new MemberTopicKey(1L, 10L),
                new MemberTopicKey(2L, 20L)
        );
    }

    @Test
    void issue_history는_100개_이상의_member_topic_key를_정확히_조회한다() {
        List<MemberTopicKey> targetKeys = LongStream.rangeClosed(1L, 120L)
                .mapToObj(index -> new MemberTopicKey(index, index + 1000L))
                .toList();
        List<MaeilMailIssueHistory> histories = targetKeys.stream()
                .map(key -> createIssueHistory(ISSUE_DATE, key.memberId(), key.topicId()))
                .toList();
        issueHistoryRepository.saveAll(histories);
        issueHistoryRepository.save(createIssueHistory(ISSUE_DATE, 1L, 1002L));

        Set<MemberTopicKey> issuedKeys = issueHistoryRepository.findIssuedMemberTopicKeys(ISSUE_DATE, targetKeys);

        assertSoftly(softly -> {
            softly.assertThat(issuedKeys).hasSize(120);
            softly.assertThat(issuedKeys).containsExactlyInAnyOrderElementsOf(targetKeys);
        });
    }

    private MaeilMailSentContent createSentContent(Long memberId, Long topicId, Long contentId) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }

    private MaeilMailIssueHistory createIssueHistory(LocalDate issueDate, Long memberId, Long topicId) {
        return MaeilMailIssueHistory.builder()
                .issueDate(issueDate)
                .memberId(memberId)
                .topicId(topicId)
                .build();
    }
}
