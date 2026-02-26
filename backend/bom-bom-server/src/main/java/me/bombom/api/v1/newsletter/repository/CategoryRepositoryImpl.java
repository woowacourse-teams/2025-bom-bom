package me.bombom.api.v1.newsletter.repository;

import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.dto.CategoryResponse;
import me.bombom.api.v1.newsletter.dto.QCategoryResponse;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CustomCategoryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CategoryResponse> findCategories(boolean includeSuspended, LocalDate thresholdDate) {
        BooleanExpression statusCondition = createStatusCondition(includeSuspended, thresholdDate);

        return jpaQueryFactory
                .select(new QCategoryResponse(category.id, category.name))
                .from(category)
                .where(JPAExpressions.selectOne()
                        .from(newsletter)
                        .where(newsletter.categoryId.eq(category.id).and(statusCondition))
                        .exists())
                .fetch();
    }

    private BooleanExpression createStatusCondition(boolean includeSuspended, LocalDate thresholdDate) {
        BooleanExpression activeOnly = newsletter.status.eq(NewsletterPublicationStatus.ACTIVE);
        if (!includeSuspended) {
            return activeOnly;
        }

        BooleanExpression suspendedVisibleCondition = newsletter.status.eq(NewsletterPublicationStatus.SUSPENDED)
                .and(newsletter.suspendedAt.isNull()
                        .or(newsletter.suspendedAt.goe(thresholdDate)));
        return activeOnly.or(suspendedVisibleCondition);
    }
}
