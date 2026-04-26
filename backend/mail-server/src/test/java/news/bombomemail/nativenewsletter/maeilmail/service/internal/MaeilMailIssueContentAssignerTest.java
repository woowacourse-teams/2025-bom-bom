package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.dto.MemberTopicKey;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueContentAssignerTest {

    @Mock
    private MaeilMailSentContentRepository sentContentRepository;

    @InjectMocks
    private MaeilMailIssueContentAssigner contentAssigner;

    @Test
    void topic_content가_없으면_선택하지_않는다() {
        // when
        Optional<Long> result = contentAssigner.assignContentIdOrRecycle(
                1L,
                10L,
                Map.of(),
                Map.of()
        );

        // then
        assertThat(result).isEmpty();
        verifyNoInteractions(sentContentRepository);
    }

    @Test
    void 이미_보낸_content를_제외하고_선택한다() {
        // given
        Long memberId = 1L;
        Long topicId = 10L;
        Map<Long, List<Long>> contentIdsByTopicId = Map.of(topicId, List.of(100L, 200L, 300L));
        Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic = Map.of(
                new MemberTopicKey(memberId, topicId),
                List.of(100L, 200L)
        );

        // when
        Optional<Long> result = contentAssigner.assignContentIdOrRecycle(
                memberId,
                topicId,
                contentIdsByTopicId,
                sentContentIdsByMemberTopic
        );

        // then
        assertThat(result).hasValue(300L);
        verifyNoInteractions(sentContentRepository);
    }

    @Test
    void content를_모두_보냈으면_전송_기록을_삭제하고_다시_선택한다() {
        // given
        Long memberId = 1L;
        Long topicId = 10L;
        List<Long> contentIds = List.of(100L, 200L);
        Map<Long, List<Long>> contentIdsByTopicId = Map.of(topicId, contentIds);
        Map<MemberTopicKey, List<Long>> sentContentIdsByMemberTopic = Map.of(
                new MemberTopicKey(memberId, topicId),
                contentIds
        );

        // when
        Optional<Long> result = contentAssigner.assignContentIdOrRecycle(
                memberId,
                topicId,
                contentIdsByTopicId,
                sentContentIdsByMemberTopic
        );

        // then
        assertThat(result).hasValueSatisfying(contentId -> assertThat(contentIds).contains(contentId));
        verify(sentContentRepository).deleteByMemberIdAndTopicId(memberId, topicId);
        verify(sentContentRepository).flush();
    }
}
