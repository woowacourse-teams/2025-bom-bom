package me.bombom.api.v1.article.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.article.dto.GetArticlesOptions;
import me.bombom.api.v1.article.enums.SortOption;
import me.bombom.api.v1.article.repository.ArticleRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
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

    public Page<ArticleResponse> getArticles(
            Long memberId,
            LocalDate date,
            String categoryName,
            SortOption sortOption,
            Pageable pageable
    ) {
        validateExistMember(memberId);
        return articleRepository.findByMemberId(
                memberId,
                GetArticlesOptions.of(date, getCategoryIdByName(categoryName), sortOption),
                pageable
        );
    }

    private void validateExistMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND);
        }
    }

    private Long getCategoryIdByName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return category.getId();
    }
}
