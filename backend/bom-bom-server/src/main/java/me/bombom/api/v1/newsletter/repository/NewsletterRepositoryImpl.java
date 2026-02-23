package me.bombom.api.v1.newsletter.repository;

import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;
import static me.bombom.api.v1.newsletter.domain.QNewsletterDetail.newsletterDetail;
import static me.bombom.api.v1.subscribe.domain.QNewsletterSubscriptionCount.newsletterSubscriptionCount;
import static me.bombom.api.v1.subscribe.domain.QSubscribe.subscribe;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.domain.NewsletterStatus;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.newsletter.dto.QNewsletterResponse;

@RequiredArgsConstructor
public class NewsletterRepositoryImpl implements CustomNewsletterRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<NewsletterResponse> findNewslettersInfo(Long memberId, boolean includeSuspended) {
        BooleanExpression publicationStatusCondition = createStatusCondition(includeSuspended);
        BooleanExpression isSubscribedCondition = createIsSubscribedCondition(memberId);

        JPAQuery<NewsletterResponse> query = jpaQueryFactory
                .select(new QNewsletterResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl,
                        newsletter.description,
                        newsletterDetail.subscribeUrl,
                        category.name,
                        newsletter.status,
                        isSubscribedCondition
                ))
                .from(newsletter)
                .join(newsletterDetail).on(newsletterDetail.id.eq(newsletter.detailId))
                .leftJoin(newsletterSubscriptionCount).on(newsletterSubscriptionCount.newsletterId.eq(newsletter.id))
                .join(category).on(category.id.eq(newsletter.categoryId))
                .where(publicationStatusCondition)
                .orderBy(
                        newsletterSubscriptionCount.total.coalesce(0).desc(),
                        newsletter.name.asc()
                );

        if (memberId != null) {
            query.leftJoin(subscribe)
                    .on(subscribe.newsletterId.eq(newsletter.id)
                                    .and(subscribe.memberId.eq(memberId)));
        }

        return query.fetch();
    }

    private BooleanExpression createStatusCondition(boolean includeSuspended) {
        BooleanExpression activeOnly = newsletter.status.eq(NewsletterStatus.ACTIVE);
        if (!includeSuspended) {
            return activeOnly;
        }
        return activeOnly.or(newsletter.status.eq(NewsletterStatus.SUSPENDED));
    }

    private BooleanExpression createIsSubscribedCondition(Long memberId) {
        if (memberId == null) {
            return Expressions.FALSE;
        }
        return subscribe.id.isNotNull();
    }
}

