package me.bombom.api.v1.subscribe.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.subscribe.domain.Subscribe;
import me.bombom.api.v1.subscribe.dto.UnsubscribeResponse;
import me.bombom.api.v1.subscribe.dto.SubscribedNewsletterResponse;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        subscribeRepository.deleteAllByMemberId(memberId);
    }

    public List<SubscribedNewsletterResponse> getSubscribedNewsletters(Member member) {
        return subscribeRepository.findSubscribedByMemberId(member.getId());
    }

    @Transactional
    public UnsubscribeResponse unsubscribe(Long memberId, Long subscribeId) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));

        if (subscribe.isNotOwner(memberId)) {
            throw new UnauthorizedException(ErrorDetail.FORBIDDEN_RESOURCE);
        }

        String unsubscribeUrl = subscribe.getUnsubscribeUrl();
        subscribeRepository.delete(subscribe);

        return new UnsubscribeResponse(unsubscribeUrl);
    }
}
