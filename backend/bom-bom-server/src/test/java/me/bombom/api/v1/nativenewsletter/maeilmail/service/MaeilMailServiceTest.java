package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailIssueHistory;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailUserAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailIdealAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailInformationResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmitAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.MaeilMailSubmittedAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailIssueHistoryRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailUserAnswerRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@IntegrationTest
class MaeilMailServiceTest {

    private static final Long TOPIC_ID = 1L;
    private static final Long ARTICLE_ID = 10_001L;
    private static final Long SECOND_ARTICLE_ID = 10_002L;
    private static final Long UNKNOWN_ARTICLE_ID = 99_999L;
    private static final String CONTENT_TITLE = "Java의 GC 동작 방식은?";
    private static final String CONTENT_HTML = "<p>Java의 GC 동작 방식은?</p>";
    private static final String CONTENT_TEXT = "Java의 GC 동작 방식은?";
    private static final String CONTENT_SUMMARY = "GC 질문";
    private static final int EXPECTED_READ_TIME = 3;
    private static final String IDEAL_ANSWER = "<p>GC는 더 이상 참조되지 않는 객체를 정리합니다.</p>";
    private static final String USER_ANSWER = "GC Root에서 도달할 수 없는 객체를 수거한다.";

    @Autowired
    private MaeilMailService maeilMailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @Autowired
    private MaeilMailContentAnswerRepository contentAnswerRepository;

    @Autowired
    private MaeilMailIssueHistoryRepository issueHistoryRepository;

    @Autowired
    private MaeilMailUserAnswerRepository userAnswerRepository;

    private Member member;
    private MaeilMailContent content;
    private MaeilMailIssueHistory issueHistory;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAllInBatch();
        contentAnswerRepository.deleteAllInBatch();
        issueHistoryRepository.deleteAllInBatch();
        contentRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(TestFixture.normalMemberFixture());
        content = contentRepository.save(createContent());
        contentAnswerRepository.save(createContentAnswer(content.getId()));
        issueHistory = issueHistoryRepository.save(createIssueHistory(ARTICLE_ID, content.getId()));
    }

    @Test
    void 매일메일_모범_답변을_조회한다() {
        // given
        Long contentId = content.getId();

        // when
        MaeilMailIdealAnswerResponse response = maeilMailService.getIdealAnswer(contentId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.title()).isEqualTo(CONTENT_TITLE);
            softly.assertThat(response.answer()).isEqualTo(IDEAL_ANSWER);
        });
    }

    @Test
    void 아티클_id로_매일메일_컨텐츠_정보를_조회한다() {
        // given
        Long articleId = issueHistory.getArticleId();

        // when
        MaeilMailInformationResponse response = maeilMailService.getContentInformationByArticle(articleId);

        // then
        assertThat(response.contentId()).isEqualTo(content.getId());
    }

    @Test
    void 아티클_id로_사용자_답변을_제출하고_조회한다() {
        // given
        Long articleId = issueHistory.getArticleId();
        MaeilMailSubmitAnswerRequest request = new MaeilMailSubmitAnswerRequest(USER_ANSWER);

        // when
        maeilMailService.submitAnswer(member, articleId, request);
        MaeilMailSubmittedAnswerResponse response = maeilMailService.getSubmittedAnswer(member, articleId);

        // then
        MaeilMailUserAnswer savedAnswer = userAnswerRepository.findAll().getFirst();
        assertSoftly(softly -> {
            softly.assertThat(response.answer()).isEqualTo(USER_ANSWER);
            softly.assertThat(savedAnswer.getMemberId()).isEqualTo(member.getId());
            softly.assertThat(savedAnswer.getIssueHistoryId()).isEqualTo(issueHistory.getId());
            softly.assertThat(savedAnswer.getAnswer()).isEqualTo(USER_ANSWER);
        });
    }

    @Test
    void 같은_컨텐츠라도_발행_이력이_다르면_사용자가_다시_답변할_수_있다() {
        // given
        MaeilMailIssueHistory secondIssueHistory = issueHistoryRepository.save(
                createIssueHistory(SECOND_ARTICLE_ID, content.getId())
        );

        // when
        maeilMailService.submitAnswer(member, issueHistory.getArticleId(), new MaeilMailSubmitAnswerRequest(USER_ANSWER));
        maeilMailService.submitAnswer(member, secondIssueHistory.getArticleId(), new MaeilMailSubmitAnswerRequest(USER_ANSWER));

        // then
        assertSoftly(softly -> {
            softly.assertThat(userAnswerRepository.findAll()).hasSize(2);
            softly.assertThat(userAnswerRepository.findByMemberIdAndIssueHistoryId(member.getId(), issueHistory.getId()))
                    .isPresent();
            softly.assertThat(userAnswerRepository.findByMemberIdAndIssueHistoryId(member.getId(), secondIssueHistory.getId()))
                    .isPresent();
        });
    }

    @Test
    void 존재하지_않는_컨텐츠의_모범_답변을_조회하면_예외가_발생한다() {
        // given
        Long unknownContentId = content.getId() + 1;

        // when & then
        assertThatThrownBy(() -> maeilMailService.getIdealAnswer(unknownContentId))
                .isInstanceOfSatisfying(CIllegalArgumentException.class, exception ->
                        assertThat(exception.getErrorDetail()).isSameAs(ErrorDetail.ENTITY_NOT_FOUND)
                );
    }

    @Test
    void 모범_답변이_없는_컨텐츠를_조회하면_예외가_발생한다() {
        // given
        MaeilMailContent contentWithoutAnswer = contentRepository.save(createContent());

        // when & then
        assertThatThrownBy(() -> maeilMailService.getIdealAnswer(contentWithoutAnswer.getId()))
                .isInstanceOfSatisfying(CIllegalArgumentException.class, exception ->
                        assertThat(exception.getErrorDetail()).isSameAs(ErrorDetail.ENTITY_NOT_FOUND)
                );
    }

    @Test
    void 존재하지_않는_아티클에_답변을_제출하면_예외가_발생하고_답변은_저장되지_않는다() {
        // given
        MaeilMailSubmitAnswerRequest request = new MaeilMailSubmitAnswerRequest(USER_ANSWER);

        // when & then
        assertThatThrownBy(() -> maeilMailService.submitAnswer(member, UNKNOWN_ARTICLE_ID, request))
                .isInstanceOfSatisfying(CIllegalArgumentException.class, exception ->
                        assertThat(exception.getErrorDetail()).isSameAs(ErrorDetail.ENTITY_NOT_FOUND)
                );

        assertThat(userAnswerRepository.findAll()).isEmpty();
    }

    @Test
    void 같은_발행_이력에_중복으로_답변을_제출하면_예외가_발생하고_답변은_하나만_유지된다() {
        // given
        Long articleId = issueHistory.getArticleId();
        MaeilMailSubmitAnswerRequest request = new MaeilMailSubmitAnswerRequest(USER_ANSWER);
        maeilMailService.submitAnswer(member, articleId, request);

        // when & then
        assertThatThrownBy(() -> maeilMailService.submitAnswer(member, articleId, request))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userAnswerRepository.findAll()).hasSize(1);
    }

    @Test
    void 제출하지_않은_답변을_조회하면_예외가_발생한다() {
        // given
        Long articleId = issueHistory.getArticleId();

        // when & then
        assertThatThrownBy(() -> maeilMailService.getSubmittedAnswer(member, articleId))
                .isInstanceOfSatisfying(CIllegalArgumentException.class, exception ->
                        assertThat(exception.getErrorDetail()).isSameAs(ErrorDetail.ENTITY_NOT_FOUND)
                );
    }

    @Test
    void 존재하지_않는_아티클의_컨텐츠_정보를_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> maeilMailService.getContentInformationByArticle(UNKNOWN_ARTICLE_ID))
                .isInstanceOfSatisfying(CIllegalArgumentException.class, exception ->
                        assertThat(exception.getErrorDetail()).isSameAs(ErrorDetail.ENTITY_NOT_FOUND)
                );
    }

    private MaeilMailContent createContent() {
        return MaeilMailContent.builder()
                .topicId(TOPIC_ID)
                .title(CONTENT_TITLE)
                .content(CONTENT_HTML)
                .contentsText(CONTENT_TEXT)
                .contentsSummary(CONTENT_SUMMARY)
                .expectedReadTime(EXPECTED_READ_TIME)
                .build();
    }

    private MaeilMailContentAnswer createContentAnswer(Long contentId) {
        return MaeilMailContentAnswer.builder()
                .contentId(contentId)
                .answer(IDEAL_ANSWER)
                .build();
    }

    private MaeilMailIssueHistory createIssueHistory(Long articleId, Long contentId) {
        return MaeilMailIssueHistory.builder()
                .articleId(articleId)
                .contentId(contentId)
                .build();
    }
}
