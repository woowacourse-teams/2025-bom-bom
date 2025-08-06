package me.bombom.api.v1.highlight.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.highlight.domain.Color;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.dto.request.HighlightCreateRequest;
import me.bombom.api.v1.highlight.dto.request.HighlightLocationRequest;
import me.bombom.api.v1.highlight.dto.request.UpdateHighlightRequest;
import me.bombom.api.v1.highlight.dto.response.HighlightResponse;
import me.bombom.api.v1.highlight.repository.HighlightRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({HighlightService.class, QuerydslConfig.class})
class HighlightServiceTest {

    @Autowired
    private HighlightService highlightService;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HighlightRepository highlightRepository;

    List<Category> categories;
    List<Newsletter> newsletters;
    List<Article> articles;
    Member member;
    List<Highlight> highlights;

    @BeforeEach
    public void setup() {
        member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        categories = TestFixture.createCategories();
        categoryRepository.saveAll(categories);
        newsletters = TestFixture.createNewsletters(categories);
        newsletterRepository.saveAll(newsletters);
        articles = TestFixture.createArticles(member, newsletters);
        articleRepository.saveAll(articles);
        highlights = TestFixture.createHighlightFixtures(articles);
        highlightRepository.saveAll(highlights);
    }

    private HighlightCreateRequest createDuplicateHighlightRequest() {
        Long firstArticleId = articles.getFirst().getId();
        return new HighlightCreateRequest(
                new HighlightLocationRequest("0", "div[0]/p[0]", "10", "div[0]/p[0]"),
                firstArticleId,
                Color.from("#ffeb3b"),
                "중복된 하이라이트",
                "메모"
        );
    }

    @Test
    void 아티클_id로_하이라이트를_조회할_수_있다() {
        // given
        Long firstArticleId = articles.getFirst().getId();

        // when
        List<HighlightResponse> responses = highlightService.getHighlightsByArticleId(firstArticleId, member);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).text()).isEqualTo("첫 번째 하이라이트");
        assertThat(responses.get(1).text()).isEqualTo("두 번째 하이라이트");
        assertThat(responses.get(0).color()).isEqualTo("#ffeb3b");
        assertThat(responses.get(1).color()).isEqualTo("#4caf50");
    }

    @Test
    void 존재하지_않는_아티클_id로_조회시_예외_발생() {
        // given
        Long nonExistArticleId = 0L;

        // when & then
        assertThatThrownBy(() -> highlightService.getHighlightsByArticleId(nonExistArticleId, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 하이라이트를_생성할_수_있다() {
        // given
        Long articleId = articles.getFirst().getId();
        HighlightCreateRequest request = TestFixture.createHighlightRequest(articleId);
        long beforeCount = highlightRepository.count();

        // when
        highlightService.create(request, member);

        // then
        assertThat(highlightRepository.count()).isEqualTo(beforeCount + 1);
    }

    @Test
    void 중복된_위치에_하이라이트_생성시_무시된다() {
        // given
        HighlightCreateRequest duplicateRequest = createDuplicateHighlightRequest();
        long beforeCount = highlightRepository.count();

        // when
        highlightService.create(duplicateRequest, member);

        // then
        assertThat(highlightRepository.count()).isEqualTo(beforeCount); // 개수 변화 없음
    }

    @Test
    void 존재하지_않는_아티클에_하이라이트_생성시_예외_발생() {
        // given
        Long nonExistentArticleId = 0L;
        HighlightCreateRequest request = TestFixture.createHighlightRequest(nonExistentArticleId);

        // when & then
        assertThatThrownBy(() -> highlightService.create(request, member))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 하이라이트를_삭제할_수_있다() {
        // given
        Long highlightId = highlights.getFirst().getId();
        long beforeCount = highlightRepository.count();

        // when
        highlightService.delete(highlightId, member);

        // then
        assertSoftly(softly -> {
            softly.assertThat(highlightRepository.count()).isEqualTo(beforeCount - 1);
            softly.assertThat(highlightRepository.findById(highlightId)).isEmpty();
        });
    }

    @Test
    void 하이라이트_색상을_변경할_수_있다() {
        // given
        Long highlightId = highlights.getFirst().getId();
        UpdateHighlightRequest request = new UpdateHighlightRequest(Color.from("#9c27b0"), null);

        // when
        HighlightResponse updated = highlightService.update(highlightId, request, member);

        // then
        assertThat(updated.color()).isEqualTo(request.color().getValue());
    }

    @Test
    void 하이라이트_메모를_변경할_수_있다() {
        // given
        Long highlightId = highlights.getFirst().getId();
        UpdateHighlightRequest request = new UpdateHighlightRequest(null, "새로운 메모입니다.");

        // when
        HighlightResponse updated = highlightService.update(highlightId, request, member);

        // then
        assertThat(updated.memo()).isEqualTo(request.memo());
    }
    @Test
    void 하이라이트_색상과_메모를_변경할_수_있다() {
        // given
        Long highlightId = highlights.getFirst().getId();
        UpdateHighlightRequest request = new UpdateHighlightRequest(Color.from("#9c27b0"), "새로운 메모입니다.");

        // when
        HighlightResponse updated = highlightService.update(highlightId, request, member);

        // then
        assertSoftly(softly -> {
                assertThat(updated.color()).isEqualTo(request.color().getValue());
                assertThat(updated.memo()).isEqualTo(request.memo());
        });
    }


    @Test
    void 존재하지_않는_하이라이트_색상_변경시_예외_발생() {
        // given
        Long nonExistentHighlightId = 0L;
        UpdateHighlightRequest request = new UpdateHighlightRequest(Color.from("#9c27b0"), null);


        // when & then
        assertThatThrownBy(() -> highlightService.update(nonExistentHighlightId, request, member))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 멤버로_하이라이트를_조회할_수_있다() {
        // when
        List<HighlightResponse> responses = highlightService.getHighlights(member);

        // then
        assertThat(responses).hasSize(highlights.size());
    }
}
