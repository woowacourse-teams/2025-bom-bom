package me.bombom.api.v1.article.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.article.dto.ArticleDetailResponse;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticleCategoryStatisticsResponse;
import me.bombom.api.v1.article.dto.GetArticleCountPerCategoryResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final NewsletterRepository newsletterRepository;
    private final ReadingService readingService;

    public Page<ArticleResponse> getArticles(
            Member member,
            LocalDate date,
            String categoryName,
            SortOption sorted,
            String keyword,
            Pageable pageable
    ) {
        Long categoryId = findCategoryIdByName(categoryName);
        return articleRepository.findByMemberId(
                member.getId(),
                GetArticlesOptions.of(date, categoryId, sorted, keyword),
                pageable);
    }

    public ArticleDetailResponse getArticleDetail(Long id, Member member) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        validateArticleOwner(article, member.getId());
        Newsletter newsletter = newsletterRepository.findById(article.getNewsletterId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        Category category = categoryRepository.findById(newsletter.getCategoryId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return ArticleDetailResponse.of(article, newsletter, category);
    }

    @Transactional
    public void markAsRead(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        validateArticleOwner(article, member.getId());
        article.markAsRead();
        readingService.updateReadingCount(article);
    }

    public GetArticleCategoryStatisticsResponse getArticleCategoryStatistics(Member member, String keyword) {
        int totalCount = articleRepository.countAllByMemberId(member.getId(), keyword);
        List<GetArticleCountPerCategoryResponse> countResponse = categoryRepository.findAll()
                .stream()
                .map(category -> {
                    int count = articleRepository.countAllByCategoryIdAndMemberId(member.getId(), category.getId(), keyword);
                    return GetArticleCountPerCategoryResponse.of(category, count);
                })
                .toList();
        return GetArticleCategoryStatisticsResponse.of(totalCount, countResponse);
    }

    private Long findCategoryIdByName(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return category.getId();
    }

    private void validateArticleOwner(Article article, Long memberId) {
        if (article.isNotOwner(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE);
        }
    }
}
