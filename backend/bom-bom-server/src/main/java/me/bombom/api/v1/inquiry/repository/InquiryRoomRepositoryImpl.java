package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;
import static me.bombom.api.v1.inquiry.domain.QInquiryRoom.inquiryRoom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class InquiryRoomRepositoryImpl implements CustomInquiryRoomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<InquiryRoom> findRoomsByRequester(Long memberId, String guestId, Pageable pageable) {
        BooleanExpression requesterEq = requesterEq(memberId, guestId);

        List<InquiryRoom> content = jpaQueryFactory
                .selectFrom(inquiryRoom)
                .where(requesterEq)
                .orderBy(inquiryRoom.createdAt.desc(), inquiryRoom.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(inquiryRoom.count())
                .from(inquiryRoom)
                .where(requesterEq);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public boolean existsUnreadAdminMessage(Long memberId, String guestId) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(inquiryRoom)
                .join(inquiryMessage).on(inquiryMessage.roomId.eq(inquiryRoom.id))
                .where(
                        requesterEq(memberId, guestId),
                        inquiryMessage.senderType.eq(InquirySenderType.ADMIN),
                        inquiryRoom.lastReadMessageIdByUser.isNull()
                                .or(inquiryMessage.id.gt(inquiryRoom.lastReadMessageIdByUser))
                )
                .fetchFirst();

        return result != null;
    }

    private BooleanExpression requesterEq(Long memberId, String guestId) {
        return memberId != null
                ? inquiryRoom.memberId.eq(memberId)
                : inquiryRoom.guestId.eq(guestId);
    }
}
