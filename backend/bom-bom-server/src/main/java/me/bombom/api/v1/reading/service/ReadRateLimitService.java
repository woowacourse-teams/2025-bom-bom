package me.bombom.api.v1.reading.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.ReadRateLimitProperties;
import me.bombom.api.v1.reading.repository.MemberReadRateLimitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadRateLimitService {

    private final MemberReadRateLimitRepository memberReadRateLimitRepository;
    private final ReadRateLimitProperties properties;
    private final Clock clock;

    @Transactional
    public boolean checkAndConsume(Long memberId) {
        LocalDateTime now = LocalDateTime.now(clock);

        memberReadRateLimitRepository.insertIfAbsent(memberId, properties.getBucketCapacity(), now);

        int affected = memberReadRateLimitRepository.tryConsume(
                memberId,
                properties.getBucketCapacity(),
                properties.getRefillSeconds(),
                now
        );

        if (affected == 0) {
            log.warn("읽기 rate limit 초과 - memberId={}", memberId);
            return false;
        }

        return true;
    }
}
