package me.bombom.api.v1.faq.repository;

import static me.bombom.api.v1.faq.domain.QFaq.faq;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.domain.FaqCategory;
import me.bombom.api.v1.faq.dto.GetFaqsOptionsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class FaqRepositoryImpl implements CustomFaqRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Faq> findFaqs(GetFaqsOptionsRequest request, Pageable pageable) {
        List<Faq> content = jpaQueryFactory
                .selectFrom(faq)
                .where(categoryEq(request.faqCategory()))
                .orderBy(faq.createdAt.desc(), faq.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(faq.count())
                .from(faq)
                .where(categoryEq(request.faqCategory()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression categoryEq(FaqCategory category) {
        if (category == null) {
            return null;
        }
        return faq.faqCategory.eq(category);
    }
}
