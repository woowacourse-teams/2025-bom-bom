package me.bombom.api.v1.article.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.dto.request.PreviousArticleRequest;
import me.bombom.api.v1.article.dto.response.PreviousArticleDetailResponse;
import me.bombom.api.v1.article.dto.response.PreviousArticleResponse;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.article.service.strategy.PreviousArticleStrategy;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PreviousArticleService {

    private static final int PREVIOUS_ARTICLE_KEEP_COUNT = 10;
    private static final String ARCHIVE_ROLE_AUTHORITY = "ARCHIVE";

    private final PreviousArticleRepository previousArticleRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;
    private final Map<NewsletterPreviousStrategy, PreviousArticleStrategy> strategyMap;

    public PreviousArticleService(
            ArticleRepository articleRepository,
            NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository,
            PreviousArticleRepository previousArticleRepository,
            MemberRepository memberRepository,
            List<PreviousArticleStrategy> strategies
    ) {
        this.articleRepository = articleRepository;
        this.newsletterPreviousPolicyRepository = newsletterPreviousPolicyRepository;
        this.previousArticleRepository = previousArticleRepository;
        this.memberRepository = memberRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(PreviousArticleStrategy::getStrategy, Function.identity()));
    }

    public List<PreviousArticleResponse> getPreviousArticles(PreviousArticleRequest request) {
        return newsletterPreviousPolicyRepository.findByNewsletterId(request.newsletterId())
                .map(this::executeStrategy) // 정책이 있으면 전략 실행
                .orElseGet(() -> { // 정책이 없으면 빈 리스트 반환
                    log.info("뉴스레터 {}에 대한 지난 아티클 정책이 설정되지 않았습니다.", request.newsletterId());
                    return List.of();
                });
    }

    public PreviousArticleDetailResponse getPreviousArticleDetail(Long id, Member member) {
        Long memberId = member != null ? member.getId() : null;
        return previousArticleRepository.findPreviousArticleDetailById(id, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ARTICLE_ID, id)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "previousArticle"));
    }

    @Transactional
    public int cleanupOldPreviousArticles() {
        int deleted = previousArticleRepository.cleanupOldPreviousArticles(PREVIOUS_ARTICLE_KEEP_COUNT);
        log.info("previous_article 정리 완료: {}건 삭제 (isFixed=false)", deleted);
        return deleted;
    }

    @Transactional
    public void moveAdminArticles() {
        List<Member> members = memberRepository.findByRoleAuthority(ARCHIVE_ROLE_AUTHORITY);
        if (members.isEmpty()) {
            throw new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "member")
                    .addContext(ErrorContextKeys.OPERATION, "moveAdminArticles");
        }

        Long previousArticleAdminId = members.getFirst().getId();
        int copied = articleRepository.safeCopyToArchive(previousArticleAdminId);
        int deleted = articleRepository.safeDeleteArchived(previousArticleAdminId, PREVIOUS_ARTICLE_KEEP_COUNT);
        log.info("아티클 이동 완료: {}건 복사, {}건 삭제 (adminId: {})", copied, deleted, previousArticleAdminId);
    }

    private List<PreviousArticleResponse> executeStrategy(NewsletterPreviousPolicy policy) {
        PreviousArticleStrategy strategy = getStrategy(policy);
        return strategy.execute(policy.getNewsletterId(), policy.getFixedCount(), policy.getRecentCount());
    }

    private PreviousArticleStrategy getStrategy(NewsletterPreviousPolicy policy) {
        return strategyMap.getOrDefault(policy.getStrategy(), strategyMap.get(NewsletterPreviousStrategy.INACTIVE));
    }
}
