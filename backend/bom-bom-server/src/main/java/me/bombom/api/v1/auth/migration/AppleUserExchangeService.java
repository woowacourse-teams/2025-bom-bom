package me.bombom.api.v1.auth.migration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleUserExchangeService {

    private static final String APPLE_PROVIDER = "apple";

    private final MemberRepository memberRepository;
    private final AppleUserExchangeClient appleUserExchangeClient;
    private final Clock clock;

    public long preview() {
        return memberRepository.countByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNull(APPLE_PROVIDER);
    }

    public AppleUserExchangeResult exchangeAll() {
        long targetCount = preview();
        long migratedCount = 0L;
        long failedCount = 0L;
        long lastMemberId = 0L;

        while (true) {
            List<Member> members = memberRepository
                    .findFirst100ByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNullAndIdGreaterThanOrderByIdAsc(
                            APPLE_PROVIDER, lastMemberId);
            if (members.isEmpty()) {
                return new AppleUserExchangeResult(targetCount, migratedCount, failedCount);
            }

            for (Member member : members) {
                lastMemberId = member.getId();
                try {
                    String newSub = appleUserExchangeClient.exchangeSub(member.getAppleTransferSub());
                    if (!StringUtils.hasText(newSub)) {
                        throw new IllegalStateException("Apple 새 팀 sub 교환 응답에 sub 값이 없습니다.");
                    }
                    member.updateAppleSubMigration(newSub, LocalDateTime.now(clock));
                    memberRepository.save(member);
                    migratedCount++;
                } catch (RuntimeException e) {
                    failedCount++;
                    log.warn("Apple 새 팀 sub 교환 실패 - memberId: {}, 사유: {}", member.getId(), e.getMessage(), e);
                }
            }
        }
    }
}
