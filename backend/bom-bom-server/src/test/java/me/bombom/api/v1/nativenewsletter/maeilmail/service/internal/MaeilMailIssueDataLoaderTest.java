package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSentContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailSubscriptionTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.IssueData;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.TopicContentId;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueDataLoaderTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2026, 4, 24);

    @Mock
    private MaeilMailTopicRepository topicRepository;

    @Mock
    private MaeilMailContentRepository contentRepository;

    @Mock
    private MaeilMailSentContentRepository sentContentRepository;

    @Mock
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Mock
    private NewsletterRepository newsletterRepository;

    @InjectMocks
    private MaeilMailIssueDataLoader issueDataLoader;

    @Test
    void 발행에_필요한_토픽_컨텐츠_발송이력_발행이력을_모아온다() {
        Newsletter newsletter = mock(Newsletter.class);
        MaeilMailTopic javaTopic = createTopic(100L, MaeilMailTrack.BE, "Java", 0);
        MaeilMailTopic springTopic = createTopic(101L, MaeilMailTrack.BE, "Spring", 1);
        MaeilMailTopic reactTopic = createTopic(200L, MaeilMailTrack.FE, "React", 0);
        MaeilMailSubscriptionTrack backendTrack = createTrack(1L, 10L, MaeilMailTrack.BE, 1);
        MaeilMailSubscriptionTrack frontendTrack = createTrack(2L, 20L, MaeilMailTrack.FE, 0);
        MemberTopicKey backendKey = new MemberTopicKey(10L, 101L);
        MemberTopicKey frontendKey = new MemberTopicKey(20L, 200L);
        Set<MemberTopicKey> issuedKeys = Set.of(frontendKey);

        given(newsletterRepository.findBySource(NewsletterSource.MAEIL_MAIL)).willReturn(java.util.Optional.of(newsletter));
        given(newsletter.getId()).willReturn(500L);
        given(topicRepository.findAllByOrderByDisplayOrderAsc()).willReturn(List.of(javaTopic, springTopic, reactTopic));
        given(contentRepository.findContentIdsByTopicIdIn(anyCollection())).willReturn(List.of(
                new TopicContentId(101L, 1000L),
                new TopicContentId(101L, 1001L),
                new TopicContentId(200L, 2000L)
        ));
        given(sentContentRepository.findAllByMemberTopicKeys(List.of(backendKey, frontendKey))).willReturn(List.of(
                createSentContent(10L, 101L, 1000L)
        ));
        given(issueHistoryRepository.findIssuedMemberTopicKeys(ISSUE_DATE, List.of(backendKey, frontendKey)))
                .willReturn(issuedKeys);

        IssueData issueData = issueDataLoader.load(ISSUE_DATE, List.of(backendTrack, frontendTrack));

        assertSoftly(softly -> {
            softly.assertThat(issueData.newsletterId()).isEqualTo(500L);
            softly.assertThat(issueData.issueTopicsByTrackId()).containsEntry(1L, springTopic);
            softly.assertThat(issueData.issueTopicsByTrackId()).containsEntry(2L, reactTopic);
            softly.assertThat(issueData.contentIdsByTopicId().get(101L)).containsExactly(1000L, 1001L);
            softly.assertThat(issueData.contentIdsByTopicId().get(200L)).containsExactly(2000L);
            softly.assertThat(issueData.sentContentIdsByMemberTopic().get(backendKey)).containsExactly(1000L);
            softly.assertThat(issueData.issuedMemberTopicKeys()).isEqualTo(issuedKeys);
        });
    }

    @Test
    void 매일메일_뉴스레터가_없으면_예외가_발생한다() {
        given(newsletterRepository.findBySource(NewsletterSource.MAEIL_MAIL)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> issueDataLoader.load(ISSUE_DATE, List.of()))
                .isInstanceOf(CServerErrorException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    private MaeilMailSubscriptionTrack createTrack(
            Long id,
            Long memberId,
            MaeilMailTrack track,
            int curriculumIndex
    ) {
        MaeilMailSubscriptionTrack subscriptionTrack = MaeilMailSubscriptionTrack.builder()
                .subscribeId(id + 100L)
                .memberId(memberId)
                .field(track)
                .build();
        ReflectionTestUtils.setField(subscriptionTrack, "id", id);
        ReflectionTestUtils.setField(subscriptionTrack, "curriculumIndex", curriculumIndex);
        return subscriptionTrack;
    }

    private MaeilMailTopic createTopic(Long id, MaeilMailTrack track, String name, int displayOrder) {
        MaeilMailTopic topic = MaeilMailTopic.builder()
                .track(track)
                .name(name)
                .displayOrder(displayOrder)
                .build();
        ReflectionTestUtils.setField(topic, "id", id);
        return topic;
    }

    private MaeilMailSentContent createSentContent(Long memberId, Long topicId, Long contentId) {
        return MaeilMailSentContent.builder()
                .memberId(memberId)
                .topicId(topicId)
                .contentId(contentId)
                .build();
    }
}
