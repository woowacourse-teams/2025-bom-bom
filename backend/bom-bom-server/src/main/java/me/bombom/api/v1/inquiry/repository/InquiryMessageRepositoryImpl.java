package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;

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

    @Override
    public Map<Long, Long> findLatestAdminMessageIdByRoomIdIn(List<Long> roomIds) {
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
