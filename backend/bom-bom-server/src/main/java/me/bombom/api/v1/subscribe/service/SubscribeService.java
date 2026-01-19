package me.bombom.api.v1.subscribe.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;
import me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.event.UnsubscribeRequestedEvent;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        subscribeRepository.deleteAllByMemberId(memberId);
    }

    public List<SubscribedNewsletterResponse> getSubscribedNewsletters(Member member) {
        return subscribeRepository.findSubscribedByMemberId(member.getId());
    }

    @Transactional
    public void unsubscribe(Long memberId, Long subscribeId) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext("subscribeId", subscribeId));

        if (subscribe.isNotOwner(memberId)) {
            throw new UnauthorizedException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ACTUAL_OWNER_ID, subscribe.getMemberId())
                .addContext("subscribeId", subscribeId);
        }

        // 자동 취소가 불가능한 경우 사용자가 직접 구독 취소 유도 후 삭제 버튼 클릭
        if (subscribe.isFailed()) {
            log.info("구독 취소 실패 상태인 항목 강제 삭제 subscribeId: {}", subscribeId);
            subscribeRepository.delete(subscribe);
            return;
        }

        // 구독 취소 이미 진행 중
        if (subscribe.isUnsubscribing()) {
            return;
        }

        subscribe.changeStatus(SubscribeStatus.UNSUBSCRIBING);
        applicationEventPublisher.publishEvent(UnsubscribeRequestedEvent.of(
                subscribe.getId(),
                subscribe.getUnsubscribeUrl(),
                subscribe.getNewsletterId()
        ));
    }

    @Transactional
    public void handleUnsubscribeResult(Long subscribeId, boolean isSuccess) {
        if (isSuccess) {
            subscribeRepository.deleteById(subscribeId);
            return;
        }

        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                        .addContext("subscribeId", subscribeId));
        subscribe.changeStatus(SubscribeStatus.FAILED);
    }
}
