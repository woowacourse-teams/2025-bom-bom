package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;
import static me.bombom.api.v1.member.domain.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.dto.InquiryMessageWithAdmin;
import me.bombom.api.v1.inquiry.dto.QInquiryMessageWithAdmin;

@RequiredArgsConstructor
public class InquiryMessageRepositoryImpl implements CustomInquiryMessageRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<InquiryMessageWithAdmin> findMessagesByCursor(Long roomId, Long cursor, int size) {
        return jpaQueryFactory
                .select(new QInquiryMessageWithAdmin(inquiryMessage, member.nickname))
                .from(inquiryMessage)
                .leftJoin(member).on(member.id.eq(inquiryMessage.adminId))
                .where(
                        inquiryMessage.roomId.eq(roomId),
                        cursorLessThan(cursor)
                )
                .orderBy(inquiryMessage.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public Map<Long, Long> findLatestAdminMessageIdsByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = jpaQueryFactory
                .select(inquiryMessage.roomId, inquiryMessage.id.max())
                .from(inquiryMessage)
                .where(
                        inquiryMessage.roomId.in(roomIds),
                        inquiryMessage.senderType.eq(InquirySenderType.ADMIN)
                )
                .groupBy(inquiryMessage.roomId)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> Objects.requireNonNull(tuple.get(inquiryMessage.roomId)),
                        tuple -> Objects.requireNonNull(tuple.get(inquiryMessage.id.max()))
                ));
    }

    private BooleanExpression cursorLessThan(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiryMessage.id.lt(cursor);
    }
}
