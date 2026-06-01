package me.bombom.api.v1.reading.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.ReadRateLimitProperties;
import me.bombom.api.v1.reading.repository.MemberReadTokenBucketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadRateLimitService {

    private final MemberReadTokenBucketRepository memberReadTokenBucketRepository;
    private final ReadRateLimitProperties properties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryConsumeReadCountToken(Long memberId, LocalDateTime now) {
        memberReadTokenBucketRepository.insertIfAbsent(memberId, properties.bucketCapacity(), now);

        int affected = memberReadTokenBucketRepository.tryConsume(
                memberId,
                properties.bucketCapacity(),
                properties.refillSeconds(),
                now
        );

        if (affected == 0) {
            log.warn("읽기 rate limit 초과 - memberId={}", memberId);
            return false;
        }

        return true;
    }
}
