package me.bombom.api.v1.member.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.transaction.Transactional;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.bookmark.service.BookmarkService;
import me.bombom.api.v1.highlight.service.HighlightService;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadingService;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import me.bombom.api.v1.withdraw.event.WithdrawEvent;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;

@Transactional
@IntegrationTest
public class MemberWithdrawIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private ReadingService readingService;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private HighlightService highlightService;

    @MockitoBean
    private SubscribeService subscribeService;

    @Test
    void 회원_탈퇴_시_정보들을_삭제하는_메서드를_호출한다() {
        // given
        Member member = memberRepository.save(TestFixture.normalMemberFixture());

        // when
        eventPublisher.publishEvent(new WithdrawEvent(member.getId()));
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        verify(readingService, times(1)).deleteAllByMemberId(member.getId());
        verify(petService, times(1)).deleteByMemberId(member.getId());
        verify(articleService, times(1)).deleteAllByMemberId(member.getId());
        verify(bookmarkService, times(1)).deleteAllByMemberId(member.getId());
        verify(highlightService, times(1)).deleteAllByMemberId(member.getId());
        verify(subscribeService, times(1)).deleteAllByMemberId(member.getId());
    }
}
