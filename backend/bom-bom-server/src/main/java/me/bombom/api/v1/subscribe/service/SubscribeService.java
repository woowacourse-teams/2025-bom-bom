package me.bombom.api.v1.subscribe.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;
import me.bombom.api.v1.subscribe.dto.response.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.event.AutoUnsubscribeCompletedEvent;
import me.bombom.api.v1.subscribe.event.UnsubscribeRequestedEvent;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final NewsletterSubscriptionCountService newsletterSubscriptionCountService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UnsubscribeAgent unsubscribeAgent;
    private final DiscordWebhookNotifier discordNotifier;
    private final UnsubscribeRetryService unsubscribeRetryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        subscribeRepository.findAllByMemberId(memberId)
                .forEach(subscribe -> newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCountByMemberId(
                        subscribe.getNewsletterId(),
                        memberId
                ));
        subscribeRepository.bulkDeleteAllByMemberId(memberId);
    }

    @Transactional
    public void deleteByMemberIdAndNewsletterId(Long memberId, Long newsletterId) {
        subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .ifPresent(subscribe -> newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCountByMemberId(
                        subscribe.getNewsletterId(),
                        memberId
                ));
        subscribeRepository.bulkDeleteByMemberIdAndNewsletterId(memberId, newsletterId);
    }

    public List<SubscribedNewsletterResponse> getSubscribedNewsletters(Member member) {
        return subscribeRepository.findSubscribedByMemberId(member.getId());
    }

    @Transactional
    public Subscribe getOrCreate(Member member, Long newsletterId) {
        Long memberId = member.getId();
        return subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                .orElseGet(() -> saveOrGetExisting(member, newsletterId));
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
        if (subscribe.isFailedToUnsubscribe()) {
            log.info("구독 취소 실패 상태인 항목 강제 삭제 subscribeId: {}", subscribeId);
            newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCountByMemberId(
                    subscribe.getNewsletterId(),
                    subscribe.getMemberId()
            );
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
                subscribe.getNewsletterId()));
    }

    @Transactional
    public void handleUnsubscribeResult(Long subscribeId, boolean isSuccess) {
        Optional<Subscribe> subscribe = subscribeRepository.findById(subscribeId);
        if (subscribe.isEmpty()) {
            log.warn("구독 정보가 존재하지 않아 해지 결과를 반영하지 않습니다. subscribeId: {}", subscribeId);
            return;
        }

        if (isSuccess) {
            handleSuccessfulUnsubscribe(subscribe.get());
            return;
        }

        handleFailedUnsubscribe(subscribe.get());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processUnsubscribe(Long subscribeId, Long newsletterId, String unsubscribeUrl) {
        try {
            unsubscribeAgent.unsubscribe(unsubscribeUrl, newsletterId);
            applicationEventPublisher.publishEvent(AutoUnsubscribeCompletedEvent.of(subscribeId, true));
            unsubscribeRetryService.deleteIfExists(subscribeId);
        } catch (RetryableException e) {
            handleRetryableFailure(subscribeId, unsubscribeUrl, e.getMessage());
        } catch (AutoUnsubscribeFailedException e) {
            handlePermanentFailure(subscribeId, unsubscribeUrl, e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 예외가 발생했습니다.", e);
            handlePermanentFailure(subscribeId, unsubscribeUrl, "예상치 못한 예외: " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void retryUnsubscribe(Long subscribeId) {
        Optional<Subscribe> subscribeOpt = subscribeRepository.findById(subscribeId);
        if (subscribeOpt.isEmpty()) {
            log.warn("구독 정보가 존재하지 않아 재시도 항목 삭제 - subscribeId: {}", subscribeId);
            unsubscribeRetryService.deleteIfExists(subscribeId);
            return;
        }

        Subscribe subscribe = subscribeOpt.get();
        processUnsubscribe(subscribe.getId(), subscribe.getNewsletterId(), subscribe.getUnsubscribeUrl());
    }

    private Subscribe create(Long memberId, Long newsletterId) {
        return subscribeRepository.save(Subscribe.builder()
                .memberId(memberId)
                .newsletterId(newsletterId)
                .build());
    }

    private Subscribe saveOrGetExisting(Member member, Long newsletterId) {
        Long memberId = member.getId();
        try {
            Subscribe subscribe = create(memberId, newsletterId);
            newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(newsletterId, member.getBirthDate());
            return subscribe;
        } catch (DataIntegrityViolationException e) {
            log.info("구독이 이미 생성되어 기존 구독 정보를 반환합니다. memberId: {}, newsletterId: {}", memberId, newsletterId);
            return subscribeRepository.findByMemberIdAndNewsletterId(memberId, newsletterId)
                    .orElseThrow(() -> e);
        }
    }

    private void handleSuccessfulUnsubscribe(Subscribe subscribe) {
        newsletterSubscriptionCountService.decreaseNewsletterSubscriptionCountByMemberId(
                subscribe.getNewsletterId(),
                subscribe.getMemberId()
        );
        subscribeRepository.delete(subscribe);
    }

    private void handleFailedUnsubscribe(Subscribe subscribe) {
        subscribe.changeStatus(SubscribeStatus.UNSUBSCRIBE_FAILED);
    }

    private void handleRetryableFailure(Long subscribeId, String url, String errorMsg) {
        boolean scheduled = unsubscribeRetryService.scheduleRetry(subscribeId, errorMsg);
        if (!scheduled) {
            handlePermanentFailure(subscribeId, url, "최대 재시도 횟수에 도달했습니다 : " + errorMsg);
        }
    }

    private void handlePermanentFailure(Long subscribeId, String url, String errorMsg) {
        unsubscribeRetryService.deleteIfExists(subscribeId);
        applicationEventPublisher.publishEvent(AutoUnsubscribeCompletedEvent.of(subscribeId, false));
        try {
            Subscribe subscribe = subscribeRepository.findById(subscribeId)
                    .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                            .addContext(ErrorContextKeys.ENTITY_TYPE, "subscribe")
                            .addContext("subscribeId", subscribeId)
                            .addContext(ErrorContextKeys.OPERATION, "handlePermanentFailure"));

            discordNotifier.sendUnsubscribeErrorNotification(
                    errorMsg,
                    subscribe,
                    url
            );
        } catch (Exception e) {
            log.error("Failed to send unsubscribe error notification (Subscribe ID: {})", subscribeId, e);
        }
    }
}
