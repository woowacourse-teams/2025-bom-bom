package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;
import static me.bombom.api.v1.inquiry.domain.QInquiryRoom.inquiryRoom;
import static me.bombom.api.v1.member.domain.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.dto.InquiryRoomWithAssignee;
import me.bombom.api.v1.inquiry.dto.QInquiryRoomWithAssignee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class InquiryRoomRepositoryImpl implements CustomInquiryRoomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<InquiryRoomWithAssignee> findRoomsByRequester(Long memberId, String guestId, Pageable pageable) {
        BooleanExpression requesterEq = requesterEq(memberId, guestId);

        List<InquiryRoomWithAssignee> content = selectRoomWithAssignee()
                .where(requesterEq)
                .orderBy(latestMessageCreatedAt().desc(), inquiryRoom.id.desc())
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
    public Optional<InquiryRoomWithAssignee> findRoomWithAssigneeById(Long roomId) {
        return Optional.ofNullable(selectRoomWithAssignee()
                .where(inquiryRoom.id.eq(roomId))
                .fetchFirst());
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

    private JPAQuery<InquiryRoomWithAssignee> selectRoomWithAssignee() {
        return jpaQueryFactory
                .select(new QInquiryRoomWithAssignee(inquiryRoom, member.nickname))
                .from(inquiryRoom)
                .leftJoin(member).on(member.id.eq(inquiryRoom.assigneeId));
    }

    /**
     * 방의 마지막 메시지 시각입니다. 메시지가 없는 방은 방 생성 시각으로 대체합니다.
     */
    private ComparableExpressionBase<LocalDateTime> latestMessageCreatedAt() {
        return new Coalesce<>(
                LocalDateTime.class,
                JPAExpressions
                        .select(inquiryMessage.createdAt.max())
                        .from(inquiryMessage)
                        .where(inquiryMessage.roomId.eq(inquiryRoom.id)),
                inquiryRoom.createdAt)
                .getValue();
    }
}
