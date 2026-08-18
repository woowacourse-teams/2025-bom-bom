package me.bombom.api.v1.auth.migration;

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
public class AppleUserMigrationService {

    private static final String APPLE_PROVIDER = "apple";

    private final MemberRepository memberRepository;
    private final AppleUserMigrationClient appleUserMigrationClient;

    public long preview() {
        return memberRepository.countByProviderAndAppleTransferSubIsNull(APPLE_PROVIDER);
    }

    public AppleUserMigrationResult migrateAll() {
        long targetCount = preview();
        long migratedCount = 0L;
        long failedCount = 0L;
        long lastMemberId = 0L;

        while (true) {
            List<Member> members = memberRepository
                    .findFirst100ByProviderAndAppleTransferSubIsNullAndIdGreaterThanOrderByIdAsc(APPLE_PROVIDER,
                            lastMemberId);
            if (members.isEmpty()) {
                return new AppleUserMigrationResult(targetCount, migratedCount, failedCount);
            }

            for (Member member : members) {
                lastMemberId = member.getId();
                try {
                    String transferSub = appleUserMigrationClient.createTransferSub(member.getProviderId());
                    if (!StringUtils.hasText(transferSub)) {
                        throw new IllegalStateException("Apple 사용자 이전 응답에 transfer_sub 값이 없습니다.");
                    }
                    member.updateAppleTransferSub(transferSub);
                    memberRepository.save(member);
                    migratedCount++;
                } catch (RuntimeException e) {
                    failedCount++;
                    log.warn("Apple 사용자 이전 실패 - memberId: {}, sub: {}, 사유: {}",
                            member.getId(), member.getProviderId(), e.getMessage(), e);
                }
            }
        }
    }
}
