package me.bombom.api.v1.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MemberTopicKey;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailSentContentRepository;
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
    void 토픽에_컨텐츠가_없으면_빈_값을_반환한다() {
        Optional<Long> contentId = contentAssigner.assignContentIdOrRecycle(
                1L,
                10L,
                Map.of(),
                Map.of()
        );

        assertThat(contentId).isEmpty();
        verify(sentContentRepository, never()).deleteByMemberIdAndTopicId(1L, 10L);
        verify(sentContentRepository, never()).flush();
    }

    @Test
    void 아직_발송하지_않은_컨텐츠를_우선_배정한다() {
        Optional<Long> contentId = contentAssigner.assignContentIdOrRecycle(
                1L,
                10L,
                Map.of(10L, List.of(100L, 101L)),
                Map.of(new MemberTopicKey(1L, 10L), List.of(100L))
        );

        assertThat(contentId).contains(101L);
        verify(sentContentRepository, never()).deleteByMemberIdAndTopicId(1L, 10L);
        verify(sentContentRepository, never()).flush();
    }

    @Test
    void 모든_컨텐츠를_발송했으면_발송_기록을_초기화하고_다시_배정한다() {
        Optional<Long> contentId = contentAssigner.assignContentIdOrRecycle(
                1L,
                10L,
                Map.of(10L, List.of(100L)),
                Map.of(new MemberTopicKey(1L, 10L), List.of(100L))
        );

        assertThat(contentId).contains(100L);
        verify(sentContentRepository).deleteByMemberIdAndTopicId(1L, 10L);
        verify(sentContentRepository).flush();
    }
}
