package news.bombom.fcm.service.token;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.fcm.domain.ArticleArrivalNotification;
import news.bombom.fcm.domain.MemberFcmToken;
import news.bombom.fcm.repository.MemberFcmTokenRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenQueryService {

    private final MemberFcmTokenRepository fcmTokenRepository;

    public List<MemberFcmToken> resolveTokens(Long memberId) {
        List<MemberFcmToken> fcmTokens = fcmTokenRepository.findByMemberId(memberId);

        if (fcmTokens.isEmpty()) {
            log.warn("FCM 토큰이 없습니다: memberId={}", memberId);
        }

        return fcmTokens;
    }
}
