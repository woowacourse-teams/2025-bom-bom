package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;

@RequiredArgsConstructor
public class InquiryMessageRepositoryImpl implements CustomInquiryMessageRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<InquiryMessage> findMessagesByCursor(Long roomId, Long cursor, int size) {
        return jpaQueryFactory
                .selectFrom(inquiryMessage)
                .where(
                        inquiryMessage.roomId.eq(roomId),
                        cursorLessThan(cursor)
                )
                .orderBy(inquiryMessage.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression cursorLessThan(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiryMessage.id.lt(cursor);
    }
}
